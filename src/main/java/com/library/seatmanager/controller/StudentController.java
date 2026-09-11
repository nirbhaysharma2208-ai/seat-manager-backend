package com.library.seatmanager.controller;

import com.library.seatmanager.dto.HalfDayStudentResponse;
import com.library.seatmanager.dto.StudentCreateRequest;
import com.library.seatmanager.dto.StudentTableResponse;
import com.library.seatmanager.dto.StudentUpdateRequest;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.entity.Seat;
import com.library.seatmanager.entity.Student;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.LibraryRepository;
import com.library.seatmanager.repository.SeatRepository;
import com.library.seatmanager.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private LibraryRepository libraryRepo;

    @Autowired
    private AdminRepository adminRepo;


    @GetMapping("/library/{libraryId}")
    public List<StudentTableResponse> getStudentsByLibrary(
            @PathVariable Long libraryId,
            Authentication auth
    ) {
        return studentRepo
                .findBySeat_Library_IdAndActiveTrue(libraryId)
                .stream()
                .map(StudentTableResponse::from)
                .toList();
    }

    @GetMapping("/seat/{seatNumber}/library/{libraryId}")
    public ResponseEntity<Student> getStudentBySeat( Authentication auth,
            @PathVariable Long libraryId,
            @PathVariable int seatNumber) {

        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));



        return studentRepo
                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                        libraryId,
                        seatNumber
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{seatNumber}/library/{libraryId}")
    public ResponseEntity<String> updateStudent( Authentication auth,
            @PathVariable Long libraryId,
            @PathVariable int seatNumber,
            @RequestBody StudentUpdateRequest req) {


        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Student student = studentRepo
                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                        libraryId,
                        seatNumber
                )
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (req.getName() != null) {
            student.setName(req.getName());
        }

        if (req.getPhone() != null) {
            student.setPhone(req.getPhone());
        }

        if (student.getSeatNumber() != req.getSeatNumber()) {

            System.out.println("Old Seat no : " + student.getSeatNumber() + " and new Seat no : " +req.getSeatNumber() );

            try {
                // 1️⃣ Free old seat
                Seat oldSeat = seatRepo.findByLibraryIdAndSeatNumber(libraryId, student.getSeatNumber())
                        .orElseThrow(() -> new RuntimeException("Old seat not found"));
                oldSeat.setOccupied(false);
                seatRepo.save(oldSeat);

                // 2️⃣ Assign new seat
                Seat newSeat = seatRepo.findByLibraryIdAndSeatNumber(libraryId, req.getSeatNumber())
                        .orElseThrow(() -> new RuntimeException("New seat not found"));


                if (newSeat.isOccupied()) {
                    throw new RuntimeException("Seat already occupied");
                }

                newSeat.setOccupied(true);
                seatRepo.save(newSeat);
                student.setSeat(newSeat);
                student.setSeatNumber(newSeat.getSeatNumber());
            }
            catch (Exception e){
                if (req.getSeatNumber() != null) {
                    student.setSeatNumber(req.getSeatNumber());
                }
            }
        }



        if (req.getEndDate() != null) {
            student.setEndDate(req.getEndDate());
        }
        if (req.getExpireDate() != null) {
            System.out.println("request ExpiredDate  : " + req.getExpireDate());
            student.setExpiryDate(req.getExpireDate());

        }

        studentRepo.save(student);

        System.out.println("Student s : " + student.getExpiryDate());
        System.out.println("Student new Seat no : " + student.getSeatNumber());
        return ResponseEntity.ok("Student updated");
    }



//  filter the student by name , phone, seat
@GetMapping("/search")
public List<StudentTableResponse> searchStudents( Authentication auth,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer seat
    ) {


        String p = auth.getName();
        Admin admin = adminRepo.findByPhone(p)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Student> students;

        if (seat != null) {
            students = studentRepo.findBySeatNumberAndActiveTrue(seat)
                    .map(List::of)
                    .orElse(List.of());
        } else if (name != null && !name.isBlank()) {
            students = studentRepo.findByNameContainingIgnoreCaseAndActiveTrue(name);
        } else if (phone != null && !phone.isBlank()) {
            students = studentRepo.findByPhoneContainingAndActiveTrue(phone);
        } else {
            students = studentRepo.findByActiveTrue();
        }

        return students.stream()
                .map(s -> StudentTableResponse.from(s))
                .toList();
    }


    @GetMapping("/expiring-soon/{libraryId}")
    public List<StudentTableResponse> expiringSoon(
            @PathVariable Long libraryId,
            Authentication auth) {
            
        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(2);

        List<Student> list =
                studentRepo
                        .findBySeat_Library_IdAndActiveTrueAndExpiryDateBetween(
                                libraryId,
                                today,
                                limit
                        );

        System.out.println("EXPIRING SOON COUNT = " + list.size());

        return list.stream()
                .map(StudentTableResponse::from)
                .toList();
    }


    @GetMapping("/expired/{libraryId}")
    public List<StudentTableResponse> expiredStudents(
            @PathVariable Long libraryId,
            Authentication auth) {


        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
                
        LocalDate today = LocalDate.now();

        List<Student> list =
                studentRepo
                        .findBySeat_Library_IdAndActiveTrueAndExpiryDateBefore(
                                libraryId,
                                today
                        );

        System.out.println("EXPIRED COUNT = " + list.size());

        return list.stream()
                .map(StudentTableResponse::from)
                .toList();
    }


    @PostMapping("/create/library/{libraryId}")
    public ResponseEntity<String> createStudent( Authentication auth,
            @PathVariable Long libraryId,
            @RequestBody StudentCreateRequest req
    ) {

        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Library library = libraryRepo.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library not found"));

        Student student = new Student();

        student.setName(req.getName());
        student.setPhone(req.getPhone());
        student.setAmountPaid(req.getAmount());

        student.setBookingDate(LocalDate.now());
        student.setStartDate(LocalDateTime.now());
        student.setExpiryDate(LocalDate.now().plusDays(30));
        student.setActive(true);

        // 🔥 VERY IMPORTANT
        student.setLibrary(library);

        // ===============================
        // FULL DAY STUDENT
        // ===============================
        if (req.getStudentType() == Student.StudentType.FULL_DAY) {

            if (req.getSeatNumber() == null) {
                throw new RuntimeException("Seat is required for full day student");
            }

            Seat seat = seatRepo
                    .findByLibraryIdAndSeatNumber(libraryId, req.getSeatNumber())
                    .orElseThrow(() -> new RuntimeException("Seat not found in this library"));

            if (seat.isOccupied()) {
                throw new RuntimeException("Seat already occupied");
            }

            seat.setOccupied(true);
            seatRepo.save(seat);

            student.setSeat(seat);
            student.setSeatNumber(seat.getSeatNumber());
            student.setStudentType(Student.StudentType.FULL_DAY);
            student.setHalfDaySlot(null);
        }

        // ===============================
        // HALF DAY STUDENT
        // ===============================
        else if (req.getStudentType() == Student.StudentType.HALF_DAY) {

            if (req.getHalfDaySlot() == null) {
                throw new RuntimeException("Half day slot is required");
            }

            student.setSeat(null);          // ✅ no seat
            student.setSeatNumber(null);    // ✅ important (not 0)
            student.setStudentType(Student.StudentType.HALF_DAY);
            student.setHalfDaySlot(req.getHalfDaySlot());
        } else {
            throw new RuntimeException("Invalid student type");
        }

        studentRepo.save(student);

        return ResponseEntity.ok("Student created successfully");
    }


    @GetMapping("/halfday/library/{libraryId}")
    public List<HalfDayStudentResponse> getHalfDayStudents( Authentication auth,
            @PathVariable Long libraryId
    ) {

        String phone = auth.getName();
        Admin admin = adminRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
                
        List<Student> list =
                studentRepo.findByLibrary_IdAndStudentTypeAndActiveTrue(
                        libraryId,
                        Student.StudentType.HALF_DAY
                );

        System.out.println("HALF DAY STUDENTS = " + list.size());

        return list.stream()
                .map(HalfDayStudentResponse::from)
                .toList();
    }



    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");

        List<Student> students = studentRepo.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Phone");
        header.createCell(2).setCellValue("Seat");
        header.createCell(3).setCellValue("EndDate");

        int rowCount = 1;

        for (Student s : students) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(s.getName());
            row.createCell(1).setCellValue(s.getPhone());
            row.createCell(2).setCellValue(s.getSeatNumber());
            row.createCell(3).setCellValue(s.getEndDate().toString());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

   @GetMapping("/export/library/{libraryId}")
public ResponseEntity<byte[]> exportStudents(
        @PathVariable Long libraryId) {
    
 List<Student> students =
         studentRepo.findByLibraryId(libraryId);

    try (Workbook workbook = new XSSFWorkbook()) {

        Sheet sheet = workbook.createSheet("Students");

        // Header
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "ID",
                "Name",
                "Phone",
                "Seat Number",
                "Start Date",
                "End Date",
                "Amount",
                "Active"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Student data
        int rowNum = 1;

        for (Student student : students) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0)
                    .setCellValue(student.getId());

            row.createCell(1)
                    .setCellValue(student.getName());

            row.createCell(2)
                    .setCellValue(student.getPhone());

            row.createCell(3)
                    .setCellValue(student.getSeatNumber());

            if (student.getStartDate() != null) {
                row.createCell(4)
                        .setCellValue(student.getStartDate().toString());
            }

            if (student.getEndDate() != null) {
                row.createCell(5)
                        .setCellValue(student.getEndDate().toString());
            }

            row.createCell(6)
                    .setCellValue(student.getAmount());

            row.createCell(7)
                    .setCellValue(student.isActive() ? "Active" : "Expired");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convert workbook to byte[]
        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        workbook.write(outputStream);

        byte[] excelFile = outputStream.toByteArray();
         return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=students.xlsx"
            )
            .contentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            .body(excelFile);

    } catch (IOException e) {

        throw new RuntimeException(
                "Failed to export students to Excel",
                e
        );
    }  
}

    private String getString(Cell cell) {
        if (cell == null) return "";

        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }

    private int getInt(Cell cell) {
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Integer.parseInt(cell.getStringCellValue());
        }
        return 0;
    }

    private LocalDate getLocalDate(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else {
            return LocalDate.parse(cell.getStringCellValue());
        }
    }

    private LocalDateTime getLocalDateTime(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue();
        } else {
            return LocalDateTime.parse(cell.getStringCellValue().replace(" ", "T"));
        }
    }
}

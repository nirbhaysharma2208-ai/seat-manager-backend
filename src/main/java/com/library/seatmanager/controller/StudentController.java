package com.library.seatmanager.controller;

import com.library.seatmanager.dto.HalfDayStudentResponse;
import com.library.seatmanager.dto.StudentCreateRequest;
import com.library.seatmanager.dto.StudentTableResponse;
import com.library.seatmanager.dto.StudentUpdateRequest;
import com.library.seatmanager.entity.*;
import com.library.seatmanager.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private SeatChangeHistoryRepository seatChangeHistoryRepo;


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
    public ResponseEntity<String> updateStudent(
            Authentication auth,
            @PathVariable Long libraryId,
            @PathVariable int seatNumber,
            @RequestBody StudentUpdateRequest req) {

        // ==========================================
        // AUTHENTICATION
        // ==========================================

        String phone = auth.getName();

        adminRepo.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );


        // ==========================================
        // FIND CURRENT STUDENT
        // ==========================================

        Student student = studentRepo
                .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                        libraryId,
                        seatNumber
                )
                .orElseThrow(() ->
                        new RuntimeException("Student not found")
                );


        // ==========================================
        // UPDATE BASIC DETAILS
        // ==========================================

        if (req.getName() != null) {
            student.setName(req.getName());
        }

        if (req.getPhone() != null) {
            student.setPhone(req.getPhone());
        }


        // ==========================================
        // SEAT CHANGE
        // ==========================================

        Integer oldSeatNumber = student.getSeatNumber();
        Integer newSeatNumber = req.getSeatNumber();


        if (newSeatNumber != null
                && oldSeatNumber != null
                && !oldSeatNumber.equals(newSeatNumber)) {

            System.out.println(
                    "Seat change requested: "
                            + oldSeatNumber
                            + " -> "
                            + newSeatNumber
            );


            // ==========================================
            // FIND OLD SEAT
            // ==========================================

            Seat oldSeat = seatRepo
                    .findByLibraryIdAndSeatNumber(
                            libraryId,
                            oldSeatNumber
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Old seat not found"
                            )
                    );


            // ==========================================
            // FIND NEW SEAT
            // ==========================================

            Seat newSeat = seatRepo
                    .findByLibraryIdAndSeatNumber(
                            libraryId,
                            newSeatNumber
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "New seat not found"
                            )
                    );


            // ==========================================
            // CHECK NEW SEAT
            // ==========================================

            if (newSeat.isOccupied()) {

                throw new RuntimeException(
                        "Seat "
                                + newSeatNumber
                                + " is already occupied"
                );
            }


            // ==========================================
            // FREE OLD SEAT
            // ==========================================

            oldSeat.setOccupied(false);

            seatRepo.save(oldSeat);


            // ==========================================
            // OCCUPY NEW SEAT
            // ==========================================

            newSeat.setOccupied(true);

            seatRepo.save(newSeat);


            // ==========================================
            // UPDATE STUDENT
            // ==========================================

            student.setSeat(newSeat);

            student.setSeatNumber(
                    newSeat.getSeatNumber()
            );


            // ==========================================
            // SAVE SEAT CHANGE HISTORY
            // ==========================================

            SeatChangeHistory history =
                    new SeatChangeHistory();

            history.setStudent(student);

            history.setLibrary(
                    student.getLibrary()
            );

            history.setOldSeatNumber(
                    oldSeatNumber
            );

            history.setNewSeatNumber(
                    newSeatNumber
            );

            history.setChangedAt(
                    LocalDateTime.now()
            );

            seatChangeHistoryRepo.save(history);


            System.out.println(
                    "Seat history saved: "
                            + oldSeatNumber
                            + " -> "
                            + newSeatNumber
            );
        }


        // ==========================================
        // UPDATE END DATE
        // ==========================================

        if (req.getEndDate() != null) {

            student.setEndDate(
                    req.getEndDate()
            );
        }


        // ==========================================
        // UPDATE EXPIRY DATE
        // ==========================================

        if (req.getExpireDate() != null) {

            student.setExpiryDate(
                    req.getExpireDate()
            );
        }


        // ==========================================
        // SAVE STUDENT
        // ==========================================

        studentRepo.save(student);


        System.out.println(
                "Student updated: "
                        + student.getName()
        );

        System.out.println(
                "Current seat: "
                        + student.getSeatNumber()
        );


        return ResponseEntity.ok(
                "Student updated"
        );
    }


    @GetMapping("/{studentId}/seat-history/library/{libraryId}")
    public ResponseEntity<?> getSeatChangeHistory(
            Authentication auth,
            @PathVariable Long studentId,
            @PathVariable Long libraryId) {

        // ==========================================
        // AUTHENTICATION
        // ==========================================

        String phone = auth.getName();

        adminRepo.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );


        // ==========================================
        // GET HISTORY
        // ==========================================

        List<SeatChangeHistory> history =
                seatChangeHistoryRepo
                        .findByStudentIdOrderByChangedAtDesc(
                                studentId
                        );


        // ==========================================
        // CONVERT TO RESPONSE
        // ==========================================

        List<Map<String, Object>> response =
                history.stream()

                        // Only history belonging to this library
                        .filter(h ->
                                h.getLibrary() != null
                                        && h.getLibrary()
                                        .getId()
                                        .equals(libraryId)
                        )

                        .map(h -> {

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put(
                                    "id",
                                    h.getId()
                            );

                            item.put(
                                    "oldSeat",
                                    h.getOldSeatNumber()
                            );

                            item.put(
                                    "newSeat",
                                    h.getNewSeatNumber()
                            );

                            item.put(
                                    "changedAt",
                                    h.getChangedAt()
                            );

                            return item;
                        })

                        .toList();


        return ResponseEntity.ok(response);
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

    @GetMapping("/export/library/{libraryId}")
    public ResponseEntity<byte[]> exportStudents(
            @PathVariable Long libraryId) {

        List<Student> students =
                studentRepo.findByLibrary_Id(libraryId);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Students");

            /*
             * ==========================================
             * HEADER
             * ==========================================
             */

            String[] headers = {
                    "ID",
                    "Name",
                    "Phone",
                    "Seat Number",
                    "Booking Date",
                    "Expiry Date",
                    "Start Date",
                    "End Date",
                    "Amount",
                    "Amount Paid",
                    "Active",
                    "Student Type",
                    "Half Day Slot"
            };

            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(
                    HorizontalAlignment.CENTER
            );

            for (int i = 0; i < headers.length; i++) {

                Cell cell = headerRow.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(headerStyle);
            }

            CellStyle dateStyle = workbook.createCellStyle();

            CreationHelper creationHelper =
                    workbook.getCreationHelper();

            dateStyle.setDataFormat(
                    creationHelper
                            .createDataFormat()
                            .getFormat("dd/MM/yyyy")
            );


            CellStyle dateTimeStyle =
                    workbook.createCellStyle();

            dateTimeStyle.setDataFormat(
                    creationHelper
                            .createDataFormat()
                            .getFormat("dd/MM/yyyy HH:mm")
            );


            int rowNum = 1;

            for (Student student : students) {

                Row row = sheet.createRow(rowNum++);


                // ID
                if (student.getId() != null) {
                    row.createCell(0)
                            .setCellValue(student.getId());
                }


                // Name
                row.createCell(1)
                        .setCellValue(
                                student.getName() != null
                                        ? student.getName()
                                        : ""
                        );


                // Phone
                row.createCell(2)
                        .setCellValue(
                                student.getPhone() != null
                                        ? student.getPhone()
                                        : ""
                        );


                // Seat Number
                if (student.getSeatNumber() != null) {
                    row.createCell(3)
                            .setCellValue(
                                    student.getSeatNumber()
                            );
                }


                // Booking Date
                if (student.getBookingDate() != null) {

                    Cell cell = row.createCell(4);

                    cell.setCellValue(
                            java.sql.Date.valueOf(
                                    student.getBookingDate()
                            )
                    );

                    cell.setCellStyle(dateStyle);
                }


                // Expiry Date
                if (student.getExpiryDate() != null) {

                    Cell cell = row.createCell(5);

                    cell.setCellValue(
                            java.sql.Date.valueOf(
                                    student.getExpiryDate()
                            )
                    );

                    cell.setCellStyle(dateStyle);
                }


                // Start Date
                if (student.getStartDate() != null) {

                    Cell cell = row.createCell(6);

                    cell.setCellValue(
                            java.sql.Timestamp.valueOf(
                                    student.getStartDate()
                            )
                    );

                    cell.setCellStyle(dateTimeStyle);
                }


                // End Date
                if (student.getEndDate() != null) {

                    Cell cell = row.createCell(7);

                    cell.setCellValue(
                            java.sql.Timestamp.valueOf(
                                    student.getEndDate()
                            )
                    );

                    cell.setCellStyle(dateTimeStyle);
                }


                // Amount
                row.createCell(8)
                        .setCellValue(student.getAmount());


                // Amount Paid
                row.createCell(9)
                        .setCellValue(student.getAmountPaid());


                // Active
                row.createCell(10)
                        .setCellValue(
                                student.isActive()
                                        ? "Active"
                                        : "Expired"
                        );


                // Student Type
                row.createCell(11)
                        .setCellValue(
                                student.getStudentType() != null
                                        ? student.getStudentType().name()
                                        : ""
                        );


                // Half Day Slot
                row.createCell(12)
                        .setCellValue(
                                student.getHalfDaySlot() != null
                                        ? student.getHalfDaySlot().name()
                                        : ""
                        );
            }

            // Freeze header
            sheet.createFreezePane(0, 1);

            // Enable filter
            sheet.setAutoFilter(
                    new CellRangeAddress(
                            0,
                            students.size(),
                            0,
                            headers.length - 1
                    )
            );


            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {

                sheet.autoSizeColumn(i);

                // Prevent extremely wide columns
                if (sheet.getColumnWidth(i) > 12000) {
                    sheet.setColumnWidth(i, 12000);
                }
            }
            /*
             * ==========================================
             * CREATE EXCEL FILE
             * ==========================================
             */

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            workbook.write(outputStream);

            byte[] excelFile =
                    outputStream.toByteArray();

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

    @PostMapping("/import/library/{libraryId}")
    public ResponseEntity<?> importStudents(
            @PathVariable Long libraryId,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Excel file is required")
            );
        }

        try {

            Library library = libraryRepo.findById(libraryId)
                    .orElseThrow(() ->
                            new RuntimeException("Library not found: " + libraryId)
                    );

            int importedCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            List<String> imported = new ArrayList<>();
            List<String> skipped = new ArrayList<>();
            List<String> failed = new ArrayList<>();

            // Prevent duplicate seat numbers inside Excel
            Set<Integer> processedSeats = new HashSet<>();

            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);

                if (sheet.getPhysicalNumberOfRows() < 2) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Excel file contains no student data")
                    );
                }

                // ============================================================
                // READ HEADER ROW
                // ============================================================

                Row headerRow = sheet.getRow(0);

                if (headerRow == null) {
                    return ResponseEntity.badRequest().body(
                            Map.of("message", "Excel header row is missing")
                    );
                }

                Map<String, Integer> columns = new HashMap<>();

                for (Cell cell : headerRow) {

                    String header = getCellString(cell);

                    if (header != null) {

                        String normalized =
                                header.trim()
                                        .toLowerCase()
                                        .replace(" ", "")
                                        .replace("_", "");

                        columns.put(normalized, cell.getColumnIndex());
                    }
                }

                // ============================================================
                // FIND REQUIRED COLUMNS
                // ============================================================

                Integer nameColumn = findColumn(
                        columns,
                        "name",
                        "studentname",
                        "student"
                );

                Integer phoneColumn = findColumn(
                        columns,
                        "phone",
                        "phonenumber",
                        "mobile",
                        "mobilenumber"
                );

                Integer seatColumn = findColumn(
                        columns,
                        "seat",
                        "seatnumber",
                        "seatno"
                );

                Integer amountPaidColumn = findColumn(
                        columns,
                        "amountpaid",
                        "paid",
                        "paidamount"
                );


                // ============================================================
                // REQUIRED COLUMN CHECK
                // ============================================================

                if (nameColumn == null) {

                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "message",
                                    "Name column not found in Excel"
                            )
                    );
                }

                if (seatColumn == null) {

                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "message",
                                    "Seat / Seat Number column not found in Excel"
                            )
                    );
                }


                // ============================================================
                // PROCESS EVERY EXCEL ROW
                // ============================================================

                for (int rowIndex = 1;
                     rowIndex <= sheet.getLastRowNum();
                     rowIndex++) {

                    Row row = sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    int excelRowNumber = rowIndex + 1;

                    try {

                        // ====================================================
                        // READ VALUES
                        // ====================================================

                        String name =
                                getCellString(
                                        row.getCell(nameColumn)
                                );

                        String phone = null;

                        if (phoneColumn != null) {
                            phone =
                                    getCellString(
                                            row.getCell(phoneColumn)
                                    );
                        }

                        Integer seatNumber =
                                getCellInteger(
                                        row.getCell(seatColumn)
                                );

                        Integer amountPaid = null;

                        if (amountPaidColumn != null) {
                            amountPaid =
                                    getCellInteger(
                                            row.getCell(amountPaidColumn)
                                    );
                        }


                        // ====================================================
                        // EMPTY ROW
                        // ====================================================

                        if ((name == null || name.isBlank())
                                && seatNumber == null) {

                            continue;
                        }


                        // ====================================================
                        // VALIDATION
                        // ====================================================

                        if (name == null || name.isBlank()) {

                            failedCount++;

                            failed.add(
                                    "Row " + excelRowNumber +
                                            ": Student name is missing"
                            );

                            continue;
                        }

                        if (seatNumber == null) {

                            failedCount++;

                            failed.add(
                                    "Row " + excelRowNumber +
                                            ": Seat number is missing"
                            );

                            continue;
                        }

                        if (seatNumber <= 0) {

                            failedCount++;

                            failed.add(
                                    "Row " + excelRowNumber +
                                            ": Invalid seat number " +
                                            seatNumber
                            );

                            continue;
                        }


                        // ====================================================
                        // DUPLICATE SEAT IN SAME EXCEL
                        // ====================================================

                        if (!processedSeats.add(seatNumber)) {

                            skippedCount++;

                            skipped.add(
                                    "Row " + excelRowNumber +
                                            ": Seat " + seatNumber +
                                            " skipped - duplicate seat in Excel"
                            );

                            continue;
                        }


                        // ====================================================
                        // FIND SEAT
                        // ====================================================

                        Optional<Seat> seatOptional =
                                seatRepo.findByLibraryIdAndSeatNumber(
                                        libraryId,
                                        seatNumber
                                );

                        if (seatOptional.isEmpty()) {

                            failedCount++;

                            failed.add(
                                    "Row " + excelRowNumber +
                                            ": Seat " + seatNumber +
                                            " does not exist in this library"
                            );

                            continue;
                        }

                        Seat seat = seatOptional.get();


                        // ====================================================
                        // DATABASE OCCUPANCY CHECK
                        //
                        // THIS IS THE MOST IMPORTANT PART
                        // ====================================================

                        if (seat.isOccupied()) {

                            skippedCount++;

                            skipped.add(
                                    "Row " + excelRowNumber +
                                            ": Seat " + seatNumber +
                                            " skipped - already occupied"
                            );

                            continue;
                        }


                        // ====================================================
                        // SECOND SAFETY CHECK
                        // ====================================================

                        Optional<Student> existingStudent =
                                studentRepo
                                        .findBySeat_Library_IdAndSeat_SeatNumberAndActiveTrue(
                                                libraryId,
                                                seatNumber
                                        );

                        if (existingStudent.isPresent()) {

                            skippedCount++;

                            skipped.add(
                                    "Row " + excelRowNumber +
                                            ": Seat " + seatNumber +
                                            " skipped - active student already exists"
                            );

                            continue;
                        }


                        // ====================================================
                        // CREATE STUDENT
                        // ====================================================

                        Student student = new Student();

                        student.setName(name.trim());
                        student.setPhone(phone);

                        student.setLibrary(library);

                        // Same defaults as normal booking
                        student.setBookingDate(LocalDate.now());
                        student.setStartDate(LocalDateTime.now());
                        student.setExpiryDate(
                                LocalDate.now().plusDays(30)
                        );

                        student.setActive(true);

                        // FULL DAY
                        student.setStudentType(
                                Student.StudentType.FULL_DAY
                        );

                        student.setHalfDaySlot(null);

                        // Seat
                        student.setSeat(seat);
                        student.setSeatNumber(seatNumber);

                        // Amount Paid
                         student.setAmountPaid(amountPaid);



                        // ====================================================
                        // SAVE STUDENT
                        // ====================================================

                        studentRepo.save(student);


                        // ====================================================
                        // OCCUPY SEAT
                        // ====================================================

                        seat.setOccupied(true);
                        seatRepo.save(seat);


                        // ====================================================
                        // SUCCESS
                        // ====================================================

                        importedCount++;

                        imported.add(
                                "Row " + excelRowNumber +
                                        ": Seat " + seatNumber +
                                        " imported - " + name
                        );

                    } catch (Exception rowException) {

                        failedCount++;

                        String errorMessage =
                                rowException.getMessage();

                        if (errorMessage == null ||
                                errorMessage.isBlank()) {

                            errorMessage =
                                    rowException.getClass()
                                            .getSimpleName();
                        }

                        // VERY IMPORTANT:
                        // Print the REAL error in backend console
                        rowException.printStackTrace();

                        failed.add(
                                "Row " + excelRowNumber +
                                        ": " + errorMessage
                        );
                    }
                }
            }


            // ================================================================
            // RESPONSE
            // ================================================================

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put(
                    "message",
                    "Import completed"
            );

            response.put(
                    "importedCount",
                    importedCount
            );

            response.put(
                    "skippedCount",
                    skippedCount
            );

            response.put(
                    "failedCount",
                    failedCount
            );

            response.put(
                    "imported",
                    imported
            );

            response.put(
                    "skipped",
                    skipped
            );

            response.put(
                    "failed",
                    failed
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Import failed",
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : e.getClass().getSimpleName()
                            )
                    );
        }
    }
    private Integer findColumn(
            Map<String, Integer> columns,
            String... names) {

        for (String name : names) {

            String normalized =
                    name.toLowerCase()
                            .replace(" ", "")
                            .replace("_", "");

            if (columns.containsKey(normalized)) {
                return columns.get(normalized);
            }
        }

        return null;
    }
    private String getCellString(Cell cell) {

        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();

        String value = formatter.formatCellValue(cell);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
    private Integer getCellInteger(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return (int) cell.getNumericCellValue();
        }

        String value = getCellString(cell);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            // Handles values like "12" and "12.0"
            double number = Double.parseDouble(value.trim());

            return (int) number;

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Invalid seat number: " + value
            );
        }
    }
    private BigDecimal getCellBigDecimal(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return BigDecimal.valueOf(
                    cell.getNumericCellValue()
            );
        }

        String value = getCellString(cell);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return new BigDecimal(
                    value.replace(",", "").trim()
            );

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Invalid amount paid: " + value
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

package onlinereservationsystem;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class OnlineReservation {

    private static final int MIN = 1000;
    private static final int MAX = 9999;

    public static class User {
        private String username;
        private String password;
        private Scanner sc = new Scanner(System.in);

        public User() {
        }

        public String getUsername() {
            System.out.print("Enter Username: ");
            username = sc.nextLine();
            return username;
        }

        public String getPassword() {
            System.out.print("Enter Password: ");
            password = sc.nextLine();
            return password;
        }

        public void registerUser(Connection connection) {
            String registerQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(registerQuery)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Registration successful.");
                } else {
                    System.out.println("Registration failed.");
                }
            } catch (SQLException e) {
                System.err.println("SQLException: " + e.getMessage());
            }
        }

        public boolean authenticateUser(Connection connection) {
            String loginQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(loginQuery)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            } catch (SQLException e) {
                System.err.println("SQLException: " + e.getMessage());
                return false;
            }
        }
    }

    public static class PnrRecord {
        private int pnrNumber;
        private String passengerName;
        private String trainNumber;
        private String classType;
        private String journeyDate;
        private String from;
        private String to;
        private Scanner sc = new Scanner(System.in);

        public int getPnrNumber() {
            Random random = new Random();
            pnrNumber = random.nextInt(MAX - MIN + 1) + MIN;
            return pnrNumber;
        }

        public String getPassengerName() {
            System.out.print("Enter the passenger name: ");
            passengerName = sc.nextLine();
            return passengerName;
        }

        public String getTrainNumber() {
            System.out.print("Enter the train number: ");
            trainNumber = sc.nextLine();
            return trainNumber;
        }

        public String getClassType() {
            String[] classTypes = {"1A-First AC Sleeper", "2A-AC 2-tier Sleeper/FirstClasss", "3A-AC 3-TIER Sleeper/Chair Car","SL-Sleeper Class"};
            System.out.println("Select Class Type:");
            for (int i = 0; i < classTypes.length; i++) {
                System.out.println((i + 1) + ". " + classTypes[i]);
            }

            int choice;
            do {
                System.out.print("Enter your choice (1-" + classTypes.length + "): ");
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } while (choice < 1 || choice > classTypes.length);

            classType = classTypes[choice - 1];
            return classType;
        }

        public String getJourneyDate() {
            System.out.print("Enter the Journey date as 'YYYY-MM-DD' format: ");
            journeyDate = sc.nextLine();
            return journeyDate;
        }

        public String getFrom() {
            System.out.print("Enter the starting place: ");
            from = sc.nextLine();
            return from;
        }

        public String getTo() {
            System.out.print("Enter the destination place: ");
            to = sc.nextLine();
            return to;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String url = "jdbc:mysql://localhost:3306/javaprj";
        String username;
        String password;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection connection = DriverManager.getConnection(url, "root", "Root@123")) { // Use your DB credentials
                System.out.println("Database connection established.\n");

                User user = new User();
                boolean loggedIn = false;

                while (!loggedIn) {
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    int choice = sc.nextInt();
                    sc.nextLine(); // Consume newline

                    if (choice == 1) {
                        // Registration
                        username = user.getUsername();
                        password = user.getPassword();
                        user.registerUser(connection);

                    } else if (choice == 2) {
                        // Login
                        username = user.getUsername();
                        password = user.getPassword();
                        loggedIn = user.authenticateUser(connection);

                        if (loggedIn) {
                            System.out.println("Login successful.\n");

                            while (true) {
                                String insertQuery = "INSERT INTO reservations (pnr_number, passenger_name, train_number, class_type, journey_date, from_location, to_location) VALUES (?, ?, ?, ?, ?, ?, ?)";
                                String deleteQuery = "DELETE FROM reservations WHERE pnr_number = ?";
                                String showQuery = "SELECT * FROM reservations";

                                System.out.println("Enter your choice:");
                                System.out.println("1. Insert Record.");
                                System.out.println("2. Delete Record.");
                                System.out.println("3. Show All Records.");
                                System.out.println("4. Logout.");
                                int action = sc.nextInt();
                                sc.nextLine(); // Consume newline

                                if (action == 1) {
                                    PnrRecord p1 = new PnrRecord();
                                    int pnrNumber = p1.getPnrNumber();
                                    String passengerName = p1.getPassengerName();
                                    String trainNumber = p1.getTrainNumber();
                                    String classType = p1.getClassType();
                                    String journeyDate = p1.getJourneyDate();
                                    String from = p1.getFrom();
                                    String to = p1.getTo();

                                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                                        preparedStatement.setInt(1, pnrNumber);
                                        preparedStatement.setString(2, passengerName);
                                        preparedStatement.setString(3, trainNumber);
                                        preparedStatement.setString(4, classType);
                                        preparedStatement.setString(5, journeyDate);
                                        preparedStatement.setString(6, from);
                                        preparedStatement.setString(7, to);

                                        int rowsAffected = preparedStatement.executeUpdate();
                                        if (rowsAffected > 0) {
                                            System.out.println("Record added successfully.");
                                        } else {
                                            System.out.println("No records were added.");
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("SQLException: " + e.getMessage());
                                    }

                                } 
                                else if (action == 2) {
                                    // Get the PNR number for deletion
                                    System.out.print("Enter the PNR number to delete the record: ");
                                    int pnrNumber = sc.nextInt();
                                    sc.nextLine(); // Consume newline

                                    // Query to retrieve the record information
                                    String showRecordQuery = "SELECT * FROM reservations WHERE pnr_number = ?";
                                    try (PreparedStatement showRecordStmt = connection.prepareStatement(showRecordQuery)) {
                                        showRecordStmt.setInt(1, pnrNumber);
                                        ResultSet resultSet = showRecordStmt.executeQuery();

                                        if (resultSet.next()) {
                                            // Display the record information
                                            System.out.println("\nRecord information:");
                                            System.out.println("PNR Number: " + resultSet.getInt("pnr_number"));
                                            System.out.println("Passenger Name: " + resultSet.getString("passenger_name"));
                                            System.out.println("Train Number: " + resultSet.getString("train_number"));
                                            System.out.println("Class Type: " + resultSet.getString("class_type"));
                                            System.out.println("Journey Date: " + resultSet.getString("journey_date"));
                                            System.out.println("From Location: " + resultSet.getString("from_location"));
                                            System.out.println("To Location: " + resultSet.getString("to_location"));

                                            // Confirm deletion
                                            System.out.print("Do you want to delete this record? (Type 'OK' to confirm): ");
                                            String confirmation = sc.nextLine();

                                            if ("OK".equalsIgnoreCase(confirmation)) {
                                                // Proceed with deletion
                                                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                                                    deleteStmt.setInt(1, pnrNumber);

                                                    int rowsAffected = deleteStmt.executeUpdate();
                                                    if (rowsAffected > 0) {
                                                        System.out.println("Record deleted successfully.");
                                                    } else {
                                                        System.out.println("No records were deleted.");
                                                    }
                                                } catch (SQLException e) {
                                                    System.err.println("SQLException: " + e.getMessage());
                                                }
                                            } else {
                                                System.out.println("Record deletion cancelled.");
                                            }
                                        } else {
                                            System.out.println("No record found with the provided PNR number.");
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("SQLException: " + e.getMessage());
                                    }
                                }
                                else if (action == 3) {
                                    try (PreparedStatement preparedStatement = connection.prepareStatement(showQuery);
                                         ResultSet resultSet = preparedStatement.executeQuery()) {

                                        System.out.println("\nAll records printing.\n");
                                        while (resultSet.next()) {
                                            int pnrNumber = resultSet.getInt("pnr_number");
                                            String passengerName = resultSet.getString("passenger_name");
                                            String trainNumber = resultSet.getString("train_number");
                                            String classType = resultSet.getString("class_type");
                                            String journeyDate = resultSet.getString("journey_date");
                                            String fromLocation = resultSet.getString("from_location");
                                            String toLocation = resultSet.getString("to_location");

                                            System.out.println("PNR Number: " + pnrNumber);
                                            System.out.println("Passenger Name: " + passengerName);
                                            System.out.println("Train Number: " + trainNumber);
                                            System.out.println("Class Type: " + classType);
                                            System.out.println("Journey Date: " + journeyDate);
                                            System.out.println("From Location: " + fromLocation);
                                            System.out.println("To Location: " + toLocation);
                                            System.out.println();
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("SQLException: " + e.getMessage());
                                    }

                                } else if (action == 4) {
                                    System.out.println("Logging out.\n");
                                    loggedIn = false;
                                    break;

                                } else {
                                    System.out.println("Invalid Choice Entered.\n");
                                }
                            }
                        } else {
                            System.out.println("Invalid username or password.\n");
                        }

                    } else if (choice == 3) {
                        System.out.println("Exiting the program.\n");
                        break;

                    } else {
                        System.out.println("Invalid Choice Entered.\n");
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQLException: " + e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            System.err.println("Error loading JDBC driver: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
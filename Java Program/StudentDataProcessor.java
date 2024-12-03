package com.myapp;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class StudentDataProcessor {

    private static final String URL = "https://birt.eriscloud.com/interviewer/data";
    private static final String ACCESS_TOKEN = "hsue0438";

    public static void main(String[] args) {
        List<JSONObject> studentsSorted = getStudents();
        if (studentsSorted != null && !studentsSorted.isEmpty()) {
            uploadStudents(studentsSorted);
        } 
    }
   
    /**
     * Fetches a list of students from the server.
     * 
     * Sends a GET request to the specified endpoint to retrieve the student data.
     * The request includes custom headers, such as an access token and content type.
     * The response is parsed and each student record is converted to a JSONObject,
     * which is then added to a list. The list is sorted alphabetically by the 
     * first name of the students.
     * 
     * @return A list of student JSON objects, sorted by their first names.
     */
    private static List<JSONObject> getStudents() {
        List<JSONObject> students = new ArrayList<>();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-access-token", ACCESS_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream(); 
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        students.add(createStudentJson(line.split(",")));
                    }
                    students.sort(Comparator.comparing(o -> o.getString("first_name")));
                }
            } else {
                System.out.println("Failed to fetch students. Status code: " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("Failed to fetch students: " + e.getMessage());
        }
        return students;
    }

    /**
     * Creates a JSONObject representing a student from an array of student data.
     * 
     * This method takes an array of strings, where each string represents a specific
     * attribute of a student (ID, first name, last name, email, and IP address), 
     * and creates a JSONObject containing the student's details.
     * 
     * @param student An array of strings where each element contains the student's 
     *                information in the following order: ID, first name, last name, 
     *                email, and IP address.
     * @return A JSONObject containing the student's details with the keys: "id", 
     *         "first_name", "last_name", "email", and "ip_address".
     */
    private static JSONObject createStudentJson(String[] student) {
        JSONObject studentJson = new JSONObject();
        studentJson.put("last_name", student[2]);
        studentJson.put("id", student[0]);
        studentJson.put("ip_address", student[4]);
        studentJson.put("first_name", student[1]);
        studentJson.put("email", student[3]);
        return studentJson;
    }

    /**
     * Uploads a list of sorted students to the server using a compressed GZIP format.
     * 
     * This method takes a list of sorted student data, compresses it using GZIP encoding, 
     * and uploads it to the server via a PUT request. The student data is first converted 
     * to a JSON array and then compressed before being sent in the request body.
     * 
     * @param studentsSorted A list of student objects that have been 
     *                       sorted by their first names. Each object contains the student's 
     *                       details such as ID, first name, last name, email, and IP address.
     */
    private static void uploadStudents(List<JSONObject> studentsSorted) {
        try {
            JSONArray jsonArray = new JSONArray(studentsSorted);
            String data = jsonArray.toString();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(data.getBytes());
            }

            byte[] compressedData = byteArrayOutputStream.toByteArray();

            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("x-access-token", ACCESS_TOKEN);
            connection.setRequestProperty("Content-Encoding", "gzip");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(compressedData);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Students uploaded successfully!");
            } else {
                System.out.println("Failed to upload students. Status code: " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("Failed to upload students: " + e.getMessage());
        }
    }
}

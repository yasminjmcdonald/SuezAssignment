const headers = {
  'x-access-token': 'hsue0438',
  'Content-Type': 'application/json'
};

const url = 'https://birt.eriscloud.com/interviewer/data';

/**
 * Fetches a list of students from the server.
 *
 * Sends a GET request to the specified endpoint to retrieve student data.
 * Includes custom headers, such as an access token and content type.
 */
async function getStudents() {
    try {
      const response = await fetch(url, {
        headers: headers
      });
      if (!response.ok) {
        throw new Error('Network response was not ok:', response.status);
      }
      const studentCsv = await response.json();
      return studentCsv;
    } catch (error) {
      console.error('Fetch error:', error);
      throw error;  
    }
  }

  /**
   * Converts a text representation of students into an array of student objects.
   *
   * Parses the input string, splits it into individual student records, and
   * creates an array of objects where each object represents a student.
   */
  function createStudentArray(studentCsv) {
    const studentArray = [];
    studentCsv.split("\n").slice(1).forEach(student => {
        let studentData = student.split(',');
        studentArray.push({"id": studentData[0], "firstName": studentData[1], "lastName": studentData[2], "email": studentData[3], "ip": studentData[4]});
    });
    return studentArray;
  }

  /**
   * Asynchronously fetches student data, converts it into a structured array, 
   * and updates an HTML table to display the student information.
   */ 
  async function displayStudents() {
    const tableBody = document.getElementById("studentsTable").querySelector("tbody");
    const studentCsv = await getStudents();
    const studentArray = createStudentArray(studentCsv);

    tableBody.innerHTML = "";
    studentArray.forEach(student => {
      const row = document.createElement("tr");
      Object.values(student).forEach(value => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.appendChild(cell);
      });
      tableBody.appendChild(row);
    });
  }
  
  /**
   * Extracts student data from an HTML table and converts it into an array of student objects.
   */ 
  function getStudentsFromTable() {
    const table = document.getElementById("studentsTable");
    const rows = table.getElementsByTagName("tr");

    const studentTableData = [];
    for (let i = 1; i < rows.length; i++) {
        const cells = rows[i].getElementsByTagName("td");

        const student = {
            id: cells[0].textContent,
            first_name: cells[1].textContent,
            last_name: cells[2].textContent,
            email: cells[3].textContent,
            ip_address: cells[4].textContent
        };
        studentTableData.push(student);
    }
    return studentTableData;
 }

 /**
  * Sorts an array of student objects alphabetically by their first names.
  */ 
 function sortStudentTable(studentTableData) {
  return studentTableData.sort((a, b) => {
    return a.first_name.localeCompare(b.first_name);
  });
 }

/**
 * Rebuilds the HTML table with sorted student data.
 */ 
function rebuildTable(studentTableDataSorted) {
  const table = document.getElementById("studentsTable");
  const tbody = table.getElementsByTagName("tbody")[0];

  tbody.innerHTML = "";

  studentTableDataSorted.forEach(student => {
      const row = document.createElement("tr");
      Object.values(student).forEach(value => {
          const cell = document.createElement("td");
          cell.textContent = value;
          row.appendChild(cell);
      });
      if (row.textContent) {
        tbody.appendChild(row);
      }
  });
}

/**
 * Uploads the current students from the HTML table to the server.
 */
async function uploadStudents() {
  const headers = {
    'x-access-token': 'hsue0438',
    'Content-Type': 'application/json'
  };

  try {
    const response = await fetch(url, {
      headers: headers,
      method: 'PUT',
      body: getStudentsFromTable()
    });
    if (!response.ok) {
      throw new Error('Network response was not ok:', response.status);
    }
  } catch (error) {
    throw error;  
  }
}
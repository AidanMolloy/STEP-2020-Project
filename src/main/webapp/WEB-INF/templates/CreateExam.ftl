<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Create Test</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="script.js"></script>
  </head>
  <body>
    <header>
      <div class="navtop">
        <p><a  href="index.html">Homepage</a></p>
        <p><a  href="dashboard.html">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <section class="form">
        <h2>Create Exam</h2>
        <form id="makeExam" action="/createExam" method="POST">
          <label for="name">Enter Exam Name:</label><br>
          <input type="text" id="name" name="name" required><br>  
          <label for="duration">Enter Duration:</label><br>
          <input type="number" id="duration" name="duration" required>
          <input type="submit" value="Submit">
          <select name="groupName">
             <#list tests as key, value>
               <option class="group">${value}</option>
             </#list>
            </select>
        </form>
      </section>
    </main>
    <footer>
    </footer>
  </body>
</html>
# Notams

## Prerequisites

You need two things installed before you can build and run this project: **Java 21** and **Apache Maven 3.9.12**.

---

## 1. Install Java 21

### Windows
1. Go to [oracle.com/java/technologies/downloads](https://www.oracle.com/java/technologies/downloads/) and download the **Java 21** installer (`.exe`) for Windows.
2. Run the installer and follow the prompts. It will set up your `PATH` automatically.
3. Verify the install by opening a new Command Prompt and running:
   ```
   java -version
   ```
   You should see `java version "21"`.

### Mac
1. Go to [oracle.com/java/technologies/downloads](https://www.oracle.com/java/technologies/downloads/) and download the **Java 21** installer (`.dmg`) for macOS.
2. Run the installer and follow the prompts.
3. Verify the install by opening a new Terminal and running:
   ```
   java -version
   ```
   You should see `java version "21"`.

Alternatively on Mac, if you have [Homebrew](https://brew.sh) installed:
```bash
brew install --cask oracle-jdk@21
```

---

## 2. Install Maven

### Windows
1. Go to [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) and download the **Binary zip archive** (e.g. `apache-maven-3.9.12-bin.zip`).
2. Extract it to a permanent location, such as:
   ```
   C:\Program Files\Apache Maven\apache-maven-3.9.12
   ```
3. Add Maven to your PATH:
   - Press the **Windows key**, search for **"Edit the system environment variables"**, and open it.
   - Click **"Environment Variables..."** at the bottom.
   - Under **System variables**, select `Path` and click **Edit**.
   - Click **New** and add the path to Maven's `bin` folder, e.g.:
     ```
     C:\Program Files\Apache Maven\apache-maven-3.9.12\bin
     ```
   - Click OK on all windows to save.
4. Open a **new** Command Prompt and verify:
   ```
   mvn -version
   ```
   You should see `Apache Maven 3.9.12`.

### Mac
1. Go to [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) and download the **Binary tar.gz archive** (e.g. `apache-maven-3.9.12-bin.tar.gz`).
2. Extract it and move it to a permanent location:
   ```bash
   tar -xvf apache-maven-3.9.12-bin.tar.gz
   sudo mv apache-maven-3.9.12 /opt/apache-maven
   ```
3. Add Maven to your PATH by adding this line to your `~/.zshrc` (or `~/.bash_profile` if using bash):
   ```bash
   export PATH="/opt/apache-maven/bin:$PATH"
   ```
4. Reload your shell:
   ```bash
   source ~/.zshrc
   ```
5. Verify:
   ```bash
   mvn -version
   ```
   You should see `Apache Maven 3.9.12`.

Alternatively on Mac, if you have [Homebrew](https://brew.sh) installed:
```bash
brew install maven
```

---

## 3. Clone the Repository

```bash
git clone https://github.com/OUCapstoneSpring2026/notams-spring26.git
cd notams-spring26
```

---

## 4. Build the Project

Compile the code and package it into a JAR:

```bash
mvn package -DskipTests
```

The built JAR will be at `target/notams-1.0-SNAPSHOT.jar`.

---

## 5. Run the Project

```bash
java -cp target/notams-1.0-SNAPSHOT.jar com.capstone.App
```

---

## 6. Run Tests

```bash
mvn test
```

---

## Other Useful Commands

Clean the build output:
```bash
mvn clean
```

Full clean rebuild:
```bash
mvn clean package
```

## Resources
* Airport coordinates: [airports.csv from OurAirports](https://ourairports.com/data/)
  * Accessed 02/23/2026
  * Trimmed to only include domestic US airports into the format: `icao,latitude,longitude`
  * Stored as `notams/src/main/resources/airportCoords.csv`
  * If filename changes, change `AIRPORT_COORDS_FILENAME` in `notams/src/main/java/com/capstone/models/Airport.java`
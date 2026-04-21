# Notams

## Prerequisites

**Java 21** and **Apache Maven 3.9.12** must be installed before running the project.

## 1. Install Java

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

## 3. Clone the Repository

```bash
git clone https://github.com/OUCapstoneSpring2026/notams-spring26.git
cd notams-spring26/notams
```

## 4. Configure Environment Variables

This project requires FAA NOTAM API credentials. These credentials must be
stored in a `.env` file and **should not be committed to Git. Verify that 
`.env` is listed in .gitignore**.

### Create the `.env` file

Create a file named `.env` in the **`notams/` directory**, at the same level as 
the `pom.xml` file.

### Add the required variables

Add the following entries to the `.env` file:

```
CLIENT_ID=your_client_id_here
CLIENT_SECRET=your_client_secret_here

# Authentication endpoint used to obtain access token
NMS_AUTH_URL=https://api-staging.cgifederal-aim.com/v1/auth/token

# NOTAM data endpoint used to query NOTAMs
NMS_NOTAM_BASE_URL=https://api-staging.cgifederal-aim.com/nmsapi/v1/notams
```

These values are used by the application to authenticate requests to the FAA
NOTAM API.

## 5. Build the Project

Use the Development instructions for quicker compilation. Use the Production instructions to package a full build of the program.

### Development
Compile the code:

```bash
mvn compile
```

### Production
Compile the code and package it into a JAR:

```bash
mvn package -DskipTests
```

The built JAR will be at `target/notams-1.0-SNAPSHOT.jar`.

## 6. Run the Project

### Development
```bash
mvn exec:java "-Dexec.mainClass=com.capstone.App"
```

### Production
```bash
java -cp target/notams-1.0-SNAPSHOT.jar com.capstone.App
```

### Command Line Arguments
These flags can optionally be used when running the program with either method above. If any are used, then both must be provided with arguments. If no flags are used, the program will prompt the user for input.
* `--departure <ICAO>`
* `--arrival <ICAO>`

Example usage:
```bash
mvn exec:java "-Dexec.mainClass=com.capstone.App" "-Dexec.args= --departure KLAX --arrival KJFK"
```

## 7. Run Tests

```bash
mvn test
```

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
  * If filename changes, change `AIRPORT_COORDS_FILENAME` in `notams/src/main/java/com/capstone/services/AirportValidator.java`

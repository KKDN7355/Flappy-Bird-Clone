# Flappy Bird Clone

Flappy Bird Clone is a Java-based 2D arcade-style game that challenges players to navigate a bird through a series of pipes without crashing. This project is built using Java Swing for the GUI and AWT for event handling and rendering, showcasing a fun implementation of game mechanics, animation, and sound integration.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

- **Java Development Kit (JDK)**: Ensure Java 8 or later is installed.

```
# Example for Ubuntu
sudo apt install openjdk-11-jdk

# Example for Windows
Download and install from https://www.oracle.com/java/technologies/javase-downloads.html
```

### Installing

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/flappy-bird-clone.git
   cd flappy-bird-clone
   ```

2. Compile the Java files:
   ```bash
   javac FlappyBird.java
   ```

3. Run the program:
   ```bash
   java FlappyBird
   ```

## Running the Tests

### Break Down Into End-to-End Tests

Currently, this project does not include automated testing. In future versions, test cases can be added using JUnit or similar frameworks.

### And Coding Style Tests

To ensure code consistency, follow Java standard naming conventions and use tools like Checkstyle for analysis.

```
# Example
java -jar checkstyle-8.45-all.jar -c /google_checks.xml FlappyBird.java
```

## Deployment

- Package the application into a JAR file for easier distribution:
  ```bash
  jar cvfe FlappyBirdClone.jar FlappyBird *.class
  ```
- Distribute the JAR file along with instructions for running it.

## Built With

* **Java Swing** - For graphical user interface components
* **Java AWT** - For event handling and rendering

## Future Additions

- **Leaderboard Integration**:
  - Save and display high scores locally or online.

- **Gravity Inversion**:
  - Implement a feature that sits between pipes that inverts gravity.

- **Achievements**:
  - Add unlockable achievements to enhance gameplay experience.

- **Enhanced Graphics**:
  - Include more detailed animations and background elements.

- **Mobile Compatibility**:
  - Adapt the game for touchscreens and smaller resolutions.

- **Soundtrack**:
  - Add a background soundtrack to complement the sound effects.

- **Deploy**:
  - Will finally then deploy on itch.io.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

* Inspiration from the original Flappy Bird game.
* Thanks to online resources for tutorials and guides.
* Special thanks to contributors for suggestions and feedback.

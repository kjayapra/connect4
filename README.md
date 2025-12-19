# Connect 4 Game

A command-line implementation of the classic Connect 4 board game in Java with comprehensive unit testing.

## Description

Connect 4 is a two-player strategy game where players take turns dropping colored tiles into a 7-column, 6-row vertical grid. The objective is to be the first to form a horizontal, vertical, or diagonal line of four tiles.

## Features

- ✅ Two-player gameplay
- ✅ Full game logic with win/draw detection
- ✅ Input validation for moves
- ✅ Console-based interface
- ✅ Comprehensive test suite (12 tests passing)

## Project Structure

```
Connect4/
├── src/
│   ├── main/java/main/java/
│   │   ├── App.java           # Main application entry point
│   │   ├── Board.java          # Game board logic
│   │   ├── GameEngine.java     # Game state management
│   │   ├── Player.java         # Player representation
│   │   ├── Move.java           # Move data structure
│   │   ├── TileColour.java     # Enum for tile colors (RED, GOLD)
│   │   └── GameState.java      # Enum for game states
│   └── test/java/
│       ├── BoardTest.java      # Board class unit tests (7 tests)
│       └── GameEngineTest.java # GameEngine tests (5 tests)
├── pom.xml                      # Maven configuration
├── TEST_CASES.md               # Comprehensive test documentation
└── README.md                   # This file
```

## Core Classes

### Board
- Manages 6x7 game grid
- Handles tile placement and move validation
- Detects wins (horizontal, vertical, diagonal)
- Detects draw conditions

### GameEngine
- Orchestrates game flow
- Manages player turns
- Validates moves and game state transitions
- Tracks current player and winner

### Player
- Represents a player with ID and tile color
- Implements equals() and hashCode() for proper comparison

### Move
- Encapsulates move data (tile color, position, success status)
- Provides immutable move representation

## Prerequisites

- Java 11 or higher
- Maven 3.6+

## Installation

1. Clone the repository:
```bash
cd /path/to/Connect4
```

2. Install dependencies:
```bash
mvn clean install
```

## How to Run

### Play the Game

```bash
mvn compile exec:java -Dexec.mainClass="main.java.App"
```

Or compile and run directly:
```bash
javac src/main/java/main/java/*.java
java -cp src/main/java main.java.App
```

### Run Tests

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=BoardTest
mvn test -Dtest=GameEngineTest
```

## How to Play

1. The game starts with Player 1 (GOLD tiles)
2. Players alternate turns
3. On your turn, enter a column number (0-6)
4. The tile drops to the lowest available position in that column
5. First player to connect 4 tiles wins!
6. If the board fills without a winner, the game is a draw

### Example Gameplay

```
Player 1, enter column (0-6): 3
Move was valid
Player 2, enter column (0-6): 3
Move was valid
Player 1, enter column (0-6): 3
Move was valid
Player 2, enter column (0-6): 4
Move was valid
Player 1, enter column (0-6): 3
Game Ended. Winner is 1
```

## Game Rules

- **Board Size:** 6 rows × 7 columns
- **Players:** 2 (Player 1: GOLD, Player 2: RED)
- **Win Condition:** 4 consecutive tiles of the same color in any direction:
  - Horizontal (—)
  - Vertical (|)
  - Diagonal (/ or \)
- **Draw Condition:** Board is full (42 tiles) with no winner
- **Invalid Moves:**
  - Column number out of range (not 0-6)
  - Column is full
  - Playing out of turn
  - Moving after game has ended

## Testing

The project uses JUnit 5 for testing with the following coverage:

### Test Statistics
- **Total Tests:** 12
- **Passing:** 12 (100%)
- **Test Classes:** 2

### BoardTest (7 tests)
- Constructor tests (default and parameterized)
- Move validation (valid and invalid moves)
- Column availability checks
- Filled counter tracking

### GameEngineTest (5 tests)
- Game initialization
- Valid move handling
- Out-of-turn move rejection
- Win detection
- Game state transitions

### Running Tests
```bash
# Run all tests with summary
mvn test

# Run with detailed output
mvn test -X

# Generate test report
mvn surefire-report:report
```

## Development

### Adding New Features

The codebase is designed to be extensible. See `TEST_CASES.md` for comprehensive test scenarios including:
- Additional board tests (win detection in all directions)
- Draw detection tests
- Edge case handling
- Integration test scenarios

### Code Quality

- Input validation on all public methods
- Proper encapsulation with getter methods
- Immutable data structures where appropriate
- Comprehensive equals() and hashCode() implementations

## Architecture

The application follows a clean separation of concerns:

1. **Model Layer:** `Board`, `Player`, `Move`, `TileColour`, `GameState`
2. **Game Logic:** `GameEngine`
3. **Presentation:** `App` (console interface)

## Known Limitations

- Console-only interface (no GUI)
- Two human players only (no AI/bot)
- No game save/load functionality
- No move undo/redo
- No game replay or move history

## Future Enhancements

Potential features for future development:
- AI opponent (bot player)
- Graphical user interface (GUI)
- Move history and replay
- Game statistics tracking
- Multiple difficulty levels for AI
- Online multiplayer support
- Customizable board sizes

## Technical Details

- **Language:** Java 11
- **Build Tool:** Maven 3.x
- **Testing Framework:** JUnit 5.10.1
- **Package Structure:** `main.java.*`
- **Compilation Target:** Java 11

## Contributing

When contributing to this project:
1. Write tests for new features
2. Ensure all existing tests pass
3. Follow existing code style and conventions
4. Update documentation as needed

## License

This is an educational project for learning Java and test-driven development.

## Author

Karthika Jayaprakash

## Version

1.0-SNAPSHOT

---

**Quick Start Commands:**

```bash
# Install and test
mvn clean install

# Run the game
mvn compile exec:java -Dexec.mainClass="main.java.App"

# Run tests
mvn test

# Check test coverage
mvn test

# Clean build artifacts
mvn clean
```

For detailed test case documentation, see [TEST_CASES.md](TEST_CASES.md).

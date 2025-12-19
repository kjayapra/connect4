# Connect 4 Test Cases Documentation

## Table of Contents
1. [Critical Bugs to Fix First](#critical-bugs-to-fix-first)
2. [Board Class Tests](#board-class-tests)
3. [GameEngine Class Tests](#gameengine-class-tests)
4. [Player Class Tests](#player-class-tests)
5. [Move Class Tests](#move-class-tests)
6. [Enum Tests](#enum-tests)
7. [Integration Tests](#integration-tests)

---

## Critical Bugs to Fix First

Before writing tests, fix these bugs in your code:

### Bug 1: Direction Array Mutation in Board.getTileCount()
**Location:** `Board.java:62-65`

**Current Code (WRONG):**
```java
if (!positive) {
    direction[0] *= -1;  // Mutates the caller's array!
    direction[1] *= -1;
}
```

**Fixed Code:**
```java
int rowDir = positive ? direction[0] : -direction[0];
int colDir = positive ? direction[1] : -direction[1];
// Then use rowDir and colDir instead of direction[0] and direction[1]
```

**Why This Matters:** The current code modifies the direction array in place, which causes subsequent win checks to use incorrect direction values, leading to false negatives in diagonal win detection.

---

### Bug 2: Missing Input Validation
**Location:** `Board.java:17` (makeMove) and `Board.java:28` (getLastAvailable)

**Issue:** No bounds checking for column index (valid range: 0-6)

**Impact:** ArrayIndexOutOfBoundsException when invalid column is passed

**Fix:** Add validation:
```java
public Move makeMove(TileColour tile, int column) {
    if (column < 0 || column >= grid[0].length) {
        return new Move(tile, column, -1, false);
    }
    // ... rest of method
}
```

---

### Bug 3: Player Equality Issue
**Location:** `Player.java`

**Issue:** No `equals()` or `hashCode()` override. GameEngine uses `player.equals(current)` which uses reference equality.

**Impact:** Two Player objects with same ID are considered different.

**Fix:** Override equals and hashCode:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Player player = (Player) o;
    return playerId == player.playerId && colour == player.colour;
}

@Override
public int hashCode() {
    return Objects.hash(playerId, colour);
}
```

---

### Bug 4: Parameterized Constructor Doesn't Initialize filled
**Location:** `Board.java:11-14`

**Issue:** Constructor accepts a grid but doesn't count existing tiles

**Impact:** checkDraw() will be incorrect if using this constructor with a pre-filled grid

**Fix:** Calculate filled count:
```java
public Board(TileColour[][] grid) {
    this.grid = grid;
    this.filled = 0;
    for (int row = 0; row < grid.length; row++) {
        for (int col = 0; col < grid[0].length; col++) {
            if (grid[row][col] != null) {
                filled++;
            }
        }
    }
}
```

---

## Board Class Tests

Total Tests: 38+

### Constructor Tests (2 tests)

#### TC-B-001: testDefaultConstructor
**Purpose:** Verify board initializes correctly with default constructor

**Setup:** Create new Board()

**Expected Results:**
- Grid is 6 rows x 7 columns
- All cells are null
- filled counter is 0
- getLastAvailable(0) returns 5 for all columns

**Test Code:**
```java
@Test
void testDefaultConstructor() {
    Board board = new Board();
    for (int col = 0; col < 7; col++) {
        assertEquals(5, board.getLastAvailable(col));
    }
}
```

---

#### TC-B-002: testParameterizedConstructor
**Purpose:** Verify board initializes with custom grid

**Setup:**
- Create custom TileColour[6][7] grid with some pre-filled tiles
- Pass to Board constructor

**Expected Results:**
- Grid is assigned correctly
- filled counter reflects number of pre-filled tiles (after bug fix)

**Test Code:**
```java
@Test
void testParameterizedConstructor() {
    TileColour[][] customGrid = new TileColour[6][7];
    customGrid[5][0] = TileColour.RED;
    customGrid[5][1] = TileColour.GOLD;

    Board board = new Board(customGrid);
    assertEquals(4, board.getLastAvailable(0)); // Row 5 is filled
    assertEquals(4, board.getLastAvailable(1)); // Row 5 is filled
    assertEquals(5, board.getLastAvailable(2)); // Empty
}
```

---

### makeMove() Method Tests (8 tests)

#### TC-B-003: testMakeMoveOnEmptyColumn
**Purpose:** Verify tile is placed at bottom of empty column

**Setup:** Empty board

**Actions:** board.makeMove(TileColour.RED, 3)

**Expected Results:**
- Move.isSuccess() returns true
- Move.getMove() returns [5, 3] (row 5, column 3)
- Move.getTile() returns TileColour.RED
- filled counter increments to 1

**Test Code:**
```java
@Test
void testMakeMoveOnEmptyColumn() {
    Board board = new Board();
    Move move = board.makeMove(TileColour.RED, 3);

    assertTrue(move.isSuccess());
    assertArrayEquals(new int[]{5, 3}, move.getMove());
    assertEquals(TileColour.RED, move.getTile());
}
```

---

#### TC-B-004: testMakeMoveStackingTiles
**Purpose:** Verify tiles stack correctly in same column

**Setup:** Empty board

**Actions:**
- Make 3 consecutive moves in column 2

**Expected Results:**
- First move: row 5
- Second move: row 4
- Third move: row 3
- All moves successful

**Test Code:**
```java
@Test
void testMakeMoveStackingTiles() {
    Board board = new Board();

    Move move1 = board.makeMove(TileColour.RED, 2);
    Move move2 = board.makeMove(TileColour.GOLD, 2);
    Move move3 = board.makeMove(TileColour.RED, 2);

    assertEquals(5, move1.getMove()[0]);
    assertEquals(4, move2.getMove()[0]);
    assertEquals(3, move3.getMove()[0]);
    assertTrue(move1.isSuccess());
    assertTrue(move2.isSuccess());
    assertTrue(move3.isSuccess());
}
```

---

#### TC-B-005: testMakeMoveOnFullColumn
**Purpose:** Verify move is rejected when column is full

**Setup:** Empty board

**Actions:**
- Fill column 0 completely (6 tiles)
- Attempt 7th move in column 0

**Expected Results:**
- 7th move: isSuccess() returns false
- 7th move: getMove() returns [-1, 0]
- filled counter is 6 (not incremented)

**Test Code:**
```java
@Test
void testMakeMoveOnFullColumn() {
    Board board = new Board();

    // Fill column 0
    for (int i = 0; i < 6; i++) {
        board.makeMove(TileColour.RED, 0);
    }

    Move failedMove = board.makeMove(TileColour.GOLD, 0);

    assertFalse(failedMove.isSuccess());
    assertEquals(-1, failedMove.getMove()[0]);
}
```

---

#### TC-B-006: testMakeMoveAllColumns
**Purpose:** Verify all columns (0-6) accept moves

**Setup:** Empty board

**Actions:** Make one move in each column

**Expected Results:**
- All moves successful
- Each move at row 5

**Test Code:**
```java
@Test
void testMakeMoveAllColumns() {
    Board board = new Board();

    for (int col = 0; col < 7; col++) {
        Move move = board.makeMove(TileColour.RED, col);
        assertTrue(move.isSuccess());
        assertEquals(5, move.getMove()[0]);
        assertEquals(col, move.getMove()[1]);
    }
}
```

---

#### TC-B-007: testMakeMoveInvalidColumnNegative
**Purpose:** Verify graceful handling of negative column index

**Setup:** Empty board

**Actions:** board.makeMove(TileColour.RED, -1)

**Expected Results:**
- Returns Move with success=false (after bug fix)
- OR throws IllegalArgumentException

**Test Code:**
```java
@Test
void testMakeMoveInvalidColumnNegative() {
    Board board = new Board();
    Move move = board.makeMove(TileColour.RED, -1);

    assertFalse(move.isSuccess());
}
```

---

#### TC-B-008: testMakeMoveInvalidColumnTooLarge
**Purpose:** Verify graceful handling of column index >= 7

**Setup:** Empty board

**Actions:** board.makeMove(TileColour.RED, 7)

**Expected Results:**
- Returns Move with success=false (after bug fix)
- OR throws IllegalArgumentException

**Test Code:**
```java
@Test
void testMakeMoveInvalidColumnTooLarge() {
    Board board = new Board();
    Move move = board.makeMove(TileColour.RED, 7);

    assertFalse(move.isSuccess());
}
```

---

#### TC-B-009: testMakeMoveWithNullTileColour
**Purpose:** Verify handling of null tile colour

**Setup:** Empty board

**Actions:** board.makeMove(null, 0)

**Expected Results:**
- Handles gracefully (either allows or rejects)
- No NullPointerException

**Test Code:**
```java
@Test
void testMakeMoveWithNullTileColour() {
    Board board = new Board();

    assertDoesNotThrow(() -> {
        Move move = board.makeMove(null, 0);
    });
}
```

---

#### TC-B-010: testMakeMoveFilledCounter
**Purpose:** Verify filled counter increments correctly

**Setup:** Empty board

**Actions:** Make 10 successful moves

**Expected Results:**
- filled counter should be 10
- Can verify by filling board to 42 and checking draw

**Test Code:**
```java
@Test
void testMakeMoveFilledCounter() {
    Board board = new Board();

    assertFalse(board.checkDraw());

    // Fill entire board
    for (int row = 0; row < 6; row++) {
        for (int col = 0; col < 7; col++) {
            board.makeMove(TileColour.RED, col);
        }
    }

    assertTrue(board.checkDraw());
}
```

---

### getLastAvailable() Method Tests (4 tests)

#### TC-B-011: testGetLastAvailableEmptyColumn
**Purpose:** Verify returns bottom row (5) for empty column

**Setup:** Empty board

**Actions:** board.getLastAvailable(0)

**Expected Results:** Returns 5

**Test Code:**
```java
@Test
void testGetLastAvailableEmptyColumn() {
    Board board = new Board();
    assertEquals(5, board.getLastAvailable(0));
}
```

---

#### TC-B-012: testGetLastAvailablePartiallyFilled
**Purpose:** Verify returns correct row for partially filled column

**Setup:** Empty board

**Actions:**
- Place 3 tiles in column 4
- Check getLastAvailable(4)

**Expected Results:** Returns 2 (rows 5,4,3 are filled)

**Test Code:**
```java
@Test
void testGetLastAvailablePartiallyFilled() {
    Board board = new Board();

    board.makeMove(TileColour.RED, 4);
    board.makeMove(TileColour.GOLD, 4);
    board.makeMove(TileColour.RED, 4);

    assertEquals(2, board.getLastAvailable(4));
}
```

---

#### TC-B-013: testGetLastAvailableFullColumn
**Purpose:** Verify returns -1 for full column

**Setup:** Empty board

**Actions:**
- Fill column 6 completely (6 tiles)
- Check getLastAvailable(6)

**Expected Results:** Returns -1

**Test Code:**
```java
@Test
void testGetLastAvailableFullColumn() {
    Board board = new Board();

    for (int i = 0; i < 6; i++) {
        board.makeMove(TileColour.RED, 6);
    }

    assertEquals(-1, board.getLastAvailable(6));
}
```

---

#### TC-B-014: testGetLastAvailableInvalidColumn
**Purpose:** Verify handling of invalid column index

**Setup:** Empty board

**Actions:**
- board.getLastAvailable(-1)
- board.getLastAvailable(7)

**Expected Results:**
- Throws ArrayIndexOutOfBoundsException OR
- Returns -1 (after bug fix)

**Test Code:**
```java
@Test
void testGetLastAvailableInvalidColumn() {
    Board board = new Board();

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
        board.getLastAvailable(-1);
    });

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
        board.getLastAvailable(7);
    });
}
```

---

### checkWin() Method Tests (16 tests)

#### TC-B-015: testCheckWinHorizontalBottomRow
**Purpose:** Verify horizontal win detection in bottom row

**Setup:** Empty board

**Actions:** Place 4 RED tiles horizontally: columns 0,1,2,3 (all row 5)

**Expected Results:**
- First 3 moves: checkWin returns false
- 4th move: checkWin returns true

**Test Code:**
```java
@Test
void testCheckWinHorizontalBottomRow() {
    Board board = new Board();

    Move move1 = board.makeMove(TileColour.RED, 0);
    Move move2 = board.makeMove(TileColour.RED, 1);
    Move move3 = board.makeMove(TileColour.RED, 2);
    Move move4 = board.makeMove(TileColour.RED, 3);

    assertFalse(board.checkWin(move1));
    assertFalse(board.checkWin(move2));
    assertFalse(board.checkWin(move3));
    assertTrue(board.checkWin(move4));
}
```

---

#### TC-B-016: testCheckWinHorizontalMiddleRow
**Purpose:** Verify horizontal win detection in middle rows

**Setup:** Empty board

**Actions:**
- Create horizontal win at row 3 by stacking tiles

**Expected Results:** Win detected

**Test Code:**
```java
@Test
void testCheckWinHorizontalMiddleRow() {
    Board board = new Board();

    // Build up to row 3 in columns 1,2,3,4
    for (int i = 0; i < 3; i++) {
        for (int col = 1; col <= 4; col++) {
            board.makeMove(TileColour.GOLD, col);
        }
    }

    // Place winning row at row 2
    Move move1 = board.makeMove(TileColour.RED, 1);
    Move move2 = board.makeMove(TileColour.RED, 2);
    Move move3 = board.makeMove(TileColour.RED, 3);
    Move move4 = board.makeMove(TileColour.RED, 4);

    assertTrue(board.checkWin(move4));
}
```

---

#### TC-B-017: testCheckWinVertical
**Purpose:** Verify vertical win detection

**Setup:** Empty board

**Actions:** Place 4 RED tiles vertically in column 0

**Expected Results:**
- First 3 moves: no win
- 4th move: win detected

**Test Code:**
```java
@Test
void testCheckWinVertical() {
    Board board = new Board();

    Move move1 = board.makeMove(TileColour.RED, 0);
    Move move2 = board.makeMove(TileColour.RED, 0);
    Move move3 = board.makeMove(TileColour.RED, 0);
    Move move4 = board.makeMove(TileColour.RED, 0);

    assertFalse(board.checkWin(move1));
    assertFalse(board.checkWin(move2));
    assertFalse(board.checkWin(move3));
    assertTrue(board.checkWin(move4));
}
```

---

#### TC-B-018: testCheckWinDiagonalAscending
**Purpose:** Verify diagonal win (/) bottom-left to top-right

**Setup:** Empty board

**Actions:**
- Create diagonal pattern:
  - Column 0: 1 tile (GOLD)
  - Column 1: 2 tiles (RED, GOLD)
  - Column 2: 3 tiles (RED, GOLD, RED)
  - Column 3: 4 tiles (RED, GOLD, RED, RED)
- The 4 RED tiles form a diagonal at positions [5,0], [4,1], [3,2], [2,3]

**Expected Results:** Win detected on 4th RED placement

**Test Code:**
```java
@Test
void testCheckWinDiagonalAscending() {
    Board board = new Board();

    // Column 0: base
    board.makeMove(TileColour.GOLD, 0);

    // Column 1: 1 support + RED
    board.makeMove(TileColour.RED, 1);
    board.makeMove(TileColour.GOLD, 1);

    // Column 2: 2 support + RED
    board.makeMove(TileColour.RED, 2);
    board.makeMove(TileColour.GOLD, 2);
    board.makeMove(TileColour.RED, 2);

    // Column 3: 3 support + RED (winning move)
    board.makeMove(TileColour.RED, 3);
    board.makeMove(TileColour.GOLD, 3);
    board.makeMove(TileColour.RED, 3);
    Move winMove = board.makeMove(TileColour.RED, 3);

    assertTrue(board.checkWin(winMove));
}
```

---

#### TC-B-019: testCheckWinDiagonalDescending
**Purpose:** Verify diagonal win (\) top-left to bottom-right

**Setup:** Empty board

**Actions:** Create descending diagonal pattern

**Expected Results:** Win detected

**Test Code:**
```java
@Test
void testCheckWinDiagonalDescending() {
    Board board = new Board();

    // Column 3: 3 support + GOLD
    board.makeMove(TileColour.RED, 3);
    board.makeMove(TileColour.RED, 3);
    board.makeMove(TileColour.RED, 3);
    board.makeMove(TileColour.GOLD, 3);

    // Column 4: 2 support + GOLD
    board.makeMove(TileColour.RED, 4);
    board.makeMove(TileColour.RED, 4);
    board.makeMove(TileColour.GOLD, 4);

    // Column 5: 1 support + GOLD
    board.makeMove(TileColour.RED, 5);
    board.makeMove(TileColour.GOLD, 5);

    // Column 6: GOLD (winning move)
    Move winMove = board.makeMove(TileColour.GOLD, 6);

    assertTrue(board.checkWin(winMove));
}
```

---

#### TC-B-020: testCheckWinTopLeftCorner
**Purpose:** Verify win detection at top-left corner

**Setup:** Empty board

**Actions:** Create horizontal win at top row, starting column 0

**Expected Results:** Win detected

---

#### TC-B-021: testCheckWinTopRightCorner
**Purpose:** Verify win detection at top-right corner

**Setup:** Empty board

**Actions:** Create horizontal win at top row, ending column 6

**Expected Results:** Win detected

---

#### TC-B-022: testCheckWinBottomLeftCorner
**Purpose:** Verify win detection at bottom-left corner

**Setup:** Empty board

**Actions:** Create horizontal win at bottom row, starting column 0

**Expected Results:** Win detected (same as TC-B-015)

---

#### TC-B-023: testCheckWinBottomRightCorner
**Purpose:** Verify win detection at bottom-right corner

**Setup:** Empty board

**Actions:** Create horizontal win at bottom row, ending column 6

**Expected Results:** Win detected

---

#### TC-B-024: testCheckWinThreeInRow
**Purpose:** Verify 3 tiles in a row is NOT a win

**Setup:** Empty board

**Actions:** Place only 3 RED tiles horizontally

**Expected Results:** checkWin returns false

**Test Code:**
```java
@Test
void testCheckWinThreeInRow() {
    Board board = new Board();

    Move move1 = board.makeMove(TileColour.RED, 0);
    Move move2 = board.makeMove(TileColour.RED, 1);
    Move move3 = board.makeMove(TileColour.RED, 2);

    assertFalse(board.checkWin(move3));
}
```

---

#### TC-B-025: testCheckWinInterruptedSequence
**Purpose:** Verify interrupted sequence (R-R-G-R) is not a win

**Setup:** Empty board

**Actions:** Place RED-RED-GOLD-RED horizontally

**Expected Results:** No win detected

**Test Code:**
```java
@Test
void testCheckWinInterruptedSequence() {
    Board board = new Board();

    board.makeMove(TileColour.RED, 0);
    board.makeMove(TileColour.RED, 1);
    board.makeMove(TileColour.GOLD, 2);
    Move move = board.makeMove(TileColour.RED, 3);

    assertFalse(board.checkWin(move));
}
```

---

#### TC-B-026: testCheckWinUnsuccessfulMove
**Purpose:** Verify checkWin returns false for unsuccessful move

**Setup:** Empty board with full column 0

**Actions:**
- Fill column 0
- Attempt move in column 0
- Check win on failed move

**Expected Results:** checkWin returns false immediately

**Test Code:**
```java
@Test
void testCheckWinUnsuccessfulMove() {
    Board board = new Board();

    for (int i = 0; i < 6; i++) {
        board.makeMove(TileColour.RED, 0);
    }

    Move failedMove = board.makeMove(TileColour.RED, 0);

    assertFalse(board.checkWin(failedMove));
}
```

---

#### TC-B-027: testCheckWinFiveInRow
**Purpose:** Verify 5+ tiles in a row still counts as win

**Setup:** Empty board

**Actions:** Place 5 RED tiles horizontally

**Expected Results:** Win detected on 4th tile (and 5th too)

**Test Code:**
```java
@Test
void testCheckWinFiveInRow() {
    Board board = new Board();

    board.makeMove(TileColour.RED, 0);
    board.makeMove(TileColour.RED, 1);
    board.makeMove(TileColour.RED, 2);
    Move move4 = board.makeMove(TileColour.RED, 3);
    Move move5 = board.makeMove(TileColour.RED, 4);

    assertTrue(board.checkWin(move4));
    assertTrue(board.checkWin(move5));
}
```

---

#### TC-B-028: testCheckWinMiddlePlacement
**Purpose:** Verify win detected when 4th tile placed in middle of sequence

**Setup:** Empty board

**Actions:**
- Place RED at columns 0, 1, 3 (leaving gap at 2)
- Place RED at column 2 (completes sequence)

**Expected Results:** Win detected on column 2 placement

**Test Code:**
```java
@Test
void testCheckWinMiddlePlacement() {
    Board board = new Board();

    board.makeMove(TileColour.RED, 0);
    board.makeMove(TileColour.RED, 1);
    board.makeMove(TileColour.RED, 3);
    Move winMove = board.makeMove(TileColour.RED, 2);

    assertTrue(board.checkWin(winMove));
}
```

---

#### TC-B-029: testCheckWinMultiplePatterns
**Purpose:** Verify win detected when multiple winning patterns exist

**Setup:** Create board with multiple possible wins

**Actions:** Create horizontal AND vertical win simultaneously

**Expected Results:** At least one win is detected

---

#### TC-B-030: testCheckWinAllDirections
**Purpose:** Comprehensive test of all 4 directions

**Setup:** Empty board

**Actions:** Test horizontal, vertical, both diagonals separately

**Expected Results:** All 4 win types detected correctly

---

### checkDraw() Method Tests (4 tests)

#### TC-B-031: testCheckDrawEmptyBoard
**Purpose:** Verify empty board is not a draw

**Setup:** Empty board

**Actions:** board.checkDraw()

**Expected Results:** Returns false

**Test Code:**
```java
@Test
void testCheckDrawEmptyBoard() {
    Board board = new Board();
    assertFalse(board.checkDraw());
}
```

---

#### TC-B-032: testCheckDrawPartiallyFilled
**Purpose:** Verify partially filled board is not a draw

**Setup:** Empty board

**Actions:**
- Place 20 tiles randomly
- Check draw

**Expected Results:** Returns false

**Test Code:**
```java
@Test
void testCheckDrawPartiallyFilled() {
    Board board = new Board();

    for (int i = 0; i < 20; i++) {
        board.makeMove(TileColour.RED, i % 7);
    }

    assertFalse(board.checkDraw());
}
```

---

#### TC-B-033: testCheckDrawFullBoard
**Purpose:** Verify full board (42 tiles) is detected as draw

**Setup:** Empty board

**Actions:** Fill all 42 cells without creating 4-in-a-row

**Expected Results:** Returns true

**Test Code:**
```java
@Test
void testCheckDrawFullBoard() {
    Board board = new Board();

    // Alternating pattern to avoid wins: R G R G R G R
    for (int row = 0; row < 6; row++) {
        for (int col = 0; col < 7; col++) {
            TileColour color = (col % 2 == 0) ? TileColour.RED : TileColour.GOLD;
            board.makeMove(color, col);
        }
    }

    assertTrue(board.checkDraw());
}
```

---

#### TC-B-034: testCheckDrawWithWin
**Purpose:** Verify draw is not triggered when win exists

**Setup:** Empty board

**Actions:**
- Create scenario close to full board
- Final move creates both full board AND win

**Expected Results:**
- Win should be checked before draw
- This is actually a GameEngine responsibility

---

### getTileCount() Method Tests (4 tests)

#### TC-B-035: testGetTileCountPositiveDirection
**Purpose:** Verify counting tiles in positive direction

**Setup:** Board with 3 consecutive RED tiles

**Actions:** Count from first tile in positive direction

**Expected Results:** Returns 2 (finds 2 more tiles)

---

#### TC-B-036: testGetTileCountNegativeDirection
**Purpose:** Verify counting tiles in negative direction

**Setup:** Board with 3 consecutive RED tiles

**Actions:** Count from last tile in negative direction

**Expected Results:** Returns 2

---

#### TC-B-037: testGetTileCountBoundaryCheck
**Purpose:** Verify doesn't go out of bounds

**Setup:** Tiles at edge of board

**Actions:** Count past edge

**Expected Results:** Stops at boundary, doesn't throw exception

---

#### TC-B-038: testGetTileCountDirectionMutation
**Purpose:** Verify direction array is NOT mutated (after bug fix)

**Setup:** Empty board with tiles

**Actions:**
- Create int[] direction = {1, 1}
- Call getTileCount(direction, RED, false, position)
- Check if direction array changed

**Expected Results:**
- Currently: direction is [-1, -1] (BUG)
- After fix: direction is still [1, 1]

**Test Code:**
```java
@Test
void testGetTileCountDirectionMutation() {
    Board board = new Board();
    board.makeMove(TileColour.RED, 0);

    int[] direction = {1, 1};
    int[] position = {5, 0};

    board.getTileCount(direction, TileColour.RED, false, position);

    // After bug fix, direction should be unchanged
    assertArrayEquals(new int[]{1, 1}, direction);
}
```

---

## GameEngine Class Tests

Total Tests: 19+

### Constructor Tests (1 test)

#### TC-GE-001: testGameEngineConstructor
**Purpose:** Verify game initializes correctly

**Setup:** Create two players

**Actions:** Create new GameEngine(player1, player2)

**Expected Results:**
- player1 and player2 are set
- current player is player1
- state is IN_PROGRESS
- board is initialized

**Test Code:**
```java
@Test
void testGameEngineConstructor() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);

    GameEngine game = new GameEngine(p1, p2);

    // Note: Need to add getters to GameEngine to test this
    // For now, test indirectly through makeMove behavior
    String result = game.makeMove(p1, 0);
    assertEquals("Move was valid", result);
}
```

---

### makeMove() Method Tests - Valid Moves (5 tests)

#### TC-GE-002: testMakeMoveValidFirstMove
**Purpose:** Verify first valid move works

**Setup:** New game

**Actions:** player1.makeMove(0)

**Expected Results:**
- Returns "Move was valid"
- Current player switches to player2

**Test Code:**
```java
@Test
void testMakeMoveValidFirstMove() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    String result = game.makeMove(p1, 0);

    assertEquals("Move was valid", result);

    // Verify turn switched by trying p2's move
    String result2 = game.makeMove(p2, 1);
    assertEquals("Move was valid", result2);
}
```

---

#### TC-GE-003: testMakeMoveValidSecondMove
**Purpose:** Verify second player can make move

**Setup:** New game, player1 makes first move

**Actions:** player2.makeMove(1)

**Expected Results:**
- Returns "Move was valid"
- Current switches back to player1

---

#### TC-GE-004: testMakeMovePlayerAlternation
**Purpose:** Verify players alternate correctly over many moves

**Setup:** New game

**Actions:** Make 10 alternating valid moves

**Expected Results:** All succeed, players alternate

**Test Code:**
```java
@Test
void testMakeMovePlayerAlternation() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    for (int i = 0; i < 5; i++) {
        String r1 = game.makeMove(p1, 0);
        String r2 = game.makeMove(p2, 1);

        assertEquals("Move was valid", r1);
        assertEquals("Move was valid", r2);
    }
}
```

---

#### TC-GE-005: testMakeMoveAllColumns
**Purpose:** Verify all columns can be played

**Setup:** New game

**Actions:** Make moves in all 7 columns

**Expected Results:** All succeed

---

#### TC-GE-006: testMakeMoveMultipleTurns
**Purpose:** Verify game maintains state over many turns

**Setup:** New game

**Actions:** Play 20 moves

**Expected Results:** All succeed, alternation maintained

---

### makeMove() Method Tests - Invalid Moves (5 tests)

#### TC-GE-007: testMakeMoveOutOfTurn
**Purpose:** Verify out-of-turn move is rejected

**Setup:** New game (player1's turn)

**Actions:** player2 attempts move

**Expected Results:**
- Returns "Player cannot make a move."
- Current player stays player1

**Test Code:**
```java
@Test
void testMakeMoveOutOfTurn() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    String result = game.makeMove(p2, 0);

    assertEquals("Player cannot make a move.", result);

    // Verify p1 can still move (turn didn't change)
    String result2 = game.makeMove(p1, 0);
    assertEquals("Move was valid", result2);
}
```

---

#### TC-GE-008: testMakeMoveInvalidColumn
**Purpose:** Verify invalid column is rejected

**Setup:** New game

**Actions:** player1.makeMove(7) (invalid column)

**Expected Results:**
- Returns "Cannot place tile in column."
- Turn doesn't change (player1 can try again)

---

#### TC-GE-009: testMakeMoveFullColumn
**Purpose:** Verify move to full column is rejected

**Setup:** New game with one full column

**Actions:** Attempt move in full column

**Expected Results:**
- Returns "Cannot place tile in column."
- Turn doesn't change

**Test Code:**
```java
@Test
void testMakeMoveFullColumn() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // Fill column 0 (requires 12 moves, alternating)
    for (int i = 0; i < 6; i++) {
        game.makeMove(p1, 0);
        game.makeMove(p2, 0);
    }

    // Now it's p1's turn, column 0 is full
    String result = game.makeMove(p1, 0);

    assertEquals("Cannot place tile in column.", result);
}
```

---

#### TC-GE-010: testMakeMoveAfterWin
**Purpose:** Verify no moves allowed after game won

**Setup:** Game in WIN state

**Actions:** Attempt move

**Expected Results:** Returns "Game is no longer ACTIVE"

**Test Code:**
```java
@Test
void testMakeMoveAfterWin() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // Create win for p1 (4 vertical in column 0)
    game.makeMove(p1, 0); // RED
    game.makeMove(p2, 1); // GOLD
    game.makeMove(p1, 0); // RED
    game.makeMove(p2, 1); // GOLD
    game.makeMove(p1, 0); // RED
    game.makeMove(p2, 1); // GOLD
    String winMsg = game.makeMove(p1, 0); // RED wins

    assertTrue(winMsg.contains("Winner"));

    // Try to make another move
    String result = game.makeMove(p2, 2);
    assertEquals("Game is no longer ACTIVE", result);
}
```

---

#### TC-GE-011: testMakeMoveAfterDraw
**Purpose:** Verify no moves allowed after draw

**Setup:** Game in DRAW state

**Actions:** Attempt move

**Expected Results:** Returns "Game is no longer ACTIVE"

---

### makeMove() Method Tests - Win Detection (4 tests)

#### TC-GE-012: testMakeMoveWinDetectionHorizontal
**Purpose:** Verify horizontal win is detected

**Setup:** New game

**Actions:** Create horizontal 4-in-a-row

**Expected Results:**
- Returns "Game Ended. Winner is [id]"
- State changes to WIN

**Test Code:**
```java
@Test
void testMakeMoveWinDetectionHorizontal() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    game.makeMove(p1, 0); // RED
    game.makeMove(p2, 0); // GOLD (stack on 0)
    game.makeMove(p1, 1); // RED
    game.makeMove(p2, 1); // GOLD (stack on 1)
    game.makeMove(p1, 2); // RED
    game.makeMove(p2, 2); // GOLD (stack on 2)
    String result = game.makeMove(p1, 3); // RED wins horizontally

    assertTrue(result.contains("Winner is 1"));
}
```

---

#### TC-GE-013: testMakeMoveWinDetectionVertical
**Purpose:** Verify vertical win is detected

**Setup:** New game

**Actions:** Create vertical 4-in-a-row

**Expected Results:** Win message with correct winner

---

#### TC-GE-014: testMakeMoveWinDetectionDiagonal
**Purpose:** Verify diagonal win is detected

**Setup:** New game

**Actions:** Create diagonal 4-in-a-row

**Expected Results:** Win message with correct winner

---

#### TC-GE-015: testMakeMoveWinnerIsSet
**Purpose:** Verify winner field is set correctly

**Setup:** New game

**Actions:** Create win for player2

**Expected Results:**
- Winner is player2
- Message contains player2's id

---

### makeMove() Method Tests - Draw Detection (2 tests)

#### TC-GE-016: testMakeMoveDrawDetection
**Purpose:** Verify draw is detected when board fills

**Setup:** New game

**Actions:** Fill board without creating win

**Expected Results:** Returns "Game Ended in Draw."

**Test Code:**
```java
@Test
void testMakeMoveDrawDetection() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // Fill board in alternating pattern (R G R G R G R)
    String result = "";
    for (int row = 0; row < 6; row++) {
        for (int col = 0; col < 7; col++) {
            Player current = (col % 2 == 0) ? p1 : p2;
            result = game.makeMove(current, col);
        }
    }

    assertEquals("Game Ended in Draw.", result);
}
```

---

#### TC-GE-017: testMakeMoveWinBeforeDraw
**Purpose:** Verify WIN takes precedence over DRAW

**Setup:** New game

**Actions:**
- Fill board almost completely
- Final move creates both full board AND win

**Expected Results:**
- Returns win message (not draw message)
- State is WIN (not DRAW)

---

### Integration Tests (2 tests)

#### TC-GE-018: testCompleteGameWin
**Purpose:** Full game scenario ending in win

**Setup:** New game

**Actions:** Play complete game to win

**Expected Results:** All moves work, win detected correctly

---

#### TC-GE-019: testCompleteGameDraw
**Purpose:** Full game scenario ending in draw

**Setup:** New game

**Actions:** Play complete game to draw

**Expected Results:** All moves work, draw detected correctly

---

## Player Class Tests

Total Tests: 9

#### TC-P-001: testPlayerConstructor
**Purpose:** Verify constructor sets fields correctly

**Test Code:**
```java
@Test
void testPlayerConstructor() {
    Player player = new Player(42, TileColour.RED);

    assertEquals(42, player.getId());
    assertEquals(TileColour.RED, player.getTileColour());
}
```

---

#### TC-P-002: testGetTileColourRed
**Purpose:** Verify getTileColour returns RED

**Test Code:**
```java
@Test
void testGetTileColourRed() {
    Player player = new Player(1, TileColour.RED);
    assertEquals(TileColour.RED, player.getTileColour());
}
```

---

#### TC-P-003: testGetTileColourGold
**Purpose:** Verify getTileColour returns GOLD

**Test Code:**
```java
@Test
void testGetTileColourGold() {
    Player player = new Player(2, TileColour.GOLD);
    assertEquals(TileColour.GOLD, player.getTileColour());
}
```

---

#### TC-P-004: testGetId
**Purpose:** Verify getId returns correct value

**Test Code:**
```java
@Test
void testGetId() {
    Player player = new Player(999, TileColour.RED);
    assertEquals(999, player.getId());
}
```

---

#### TC-P-005: testPlayerEquality
**Purpose:** Verify equals() behavior

**Test Code:**
```java
@Test
void testPlayerEquality() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(1, TileColour.RED);
    Player p3 = new Player(2, TileColour.RED);

    // After bug fix, should be true
    // Currently false (uses Object.equals - reference equality)
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
}
```

---

#### TC-P-006: testMultiplePlayers
**Purpose:** Verify multiple player instances are independent

**Test Code:**
```java
@Test
void testMultiplePlayers() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);

    assertEquals(1, p1.getId());
    assertEquals(2, p2.getId());
    assertEquals(TileColour.RED, p1.getTileColour());
    assertEquals(TileColour.GOLD, p2.getTileColour());
}
```

---

#### TC-P-007: testNegativePlayerId
**Purpose:** Verify negative player ID is allowed

**Test Code:**
```java
@Test
void testNegativePlayerId() {
    Player player = new Player(-5, TileColour.RED);
    assertEquals(-5, player.getId());
}
```

---

#### TC-P-008: testZeroPlayerId
**Purpose:** Verify player ID of 0 is allowed

**Test Code:**
```java
@Test
void testZeroPlayerId() {
    Player player = new Player(0, TileColour.GOLD);
    assertEquals(0, player.getId());
}
```

---

#### TC-P-009: testLargePlayerId
**Purpose:** Verify large player ID works

**Test Code:**
```java
@Test
void testLargePlayerId() {
    Player player = new Player(Long.MAX_VALUE, TileColour.RED);
    assertEquals(Long.MAX_VALUE, player.getId());
}
```

---

## Move Class Tests

Total Tests: 10

#### TC-M-001: testMoveConstructor
**Purpose:** Verify constructor sets all fields

**Test Code:**
```java
@Test
void testMoveConstructor() {
    Move move = new Move(TileColour.RED, 3, 5, true);

    assertEquals(TileColour.RED, move.getTile());
    assertArrayEquals(new int[]{5, 3}, move.getMove());
    assertTrue(move.isSuccess());
}
```

---

#### TC-M-002: testGetTile
**Purpose:** Verify getTile returns correct colour

**Test Code:**
```java
@Test
void testGetTile() {
    Move move = new Move(TileColour.GOLD, 0, 0, true);
    assertEquals(TileColour.GOLD, move.getTile());
}
```

---

#### TC-M-003: testGetMove
**Purpose:** Verify getMove returns [row, column]

**Test Code:**
```java
@Test
void testGetMove() {
    Move move = new Move(TileColour.RED, 4, 2, true);
    assertArrayEquals(new int[]{2, 4}, move.getMove());
}
```

---

#### TC-M-004: testIsSuccessTrue
**Purpose:** Verify isSuccess returns true

**Test Code:**
```java
@Test
void testIsSuccessTrue() {
    Move move = new Move(TileColour.RED, 0, 0, true);
    assertTrue(move.isSuccess());
}
```

---

#### TC-M-005: testIsSuccessFalse
**Purpose:** Verify isSuccess returns false

**Test Code:**
```java
@Test
void testIsSuccessFalse() {
    Move move = new Move(TileColour.RED, 0, -1, false);
    assertFalse(move.isSuccess());
}
```

---

#### TC-M-006: testSuccessfulMove
**Purpose:** Verify successful move representation

**Test Code:**
```java
@Test
void testSuccessfulMove() {
    Move move = new Move(TileColour.GOLD, 6, 5, true);

    assertTrue(move.isSuccess());
    assertEquals(TileColour.GOLD, move.getTile());
    assertEquals(5, move.getMove()[0]);
    assertEquals(6, move.getMove()[1]);
}
```

---

#### TC-M-007: testUnsuccessfulMove
**Purpose:** Verify unsuccessful move representation

**Test Code:**
```java
@Test
void testUnsuccessfulMove() {
    Move move = new Move(TileColour.RED, 3, -1, false);

    assertFalse(move.isSuccess());
    assertEquals(-1, move.getMove()[0]);
}
```

---

#### TC-M-008: testGetMoveArrayIndependence
**Purpose:** Verify getMove creates new array each time

**Test Code:**
```java
@Test
void testGetMoveArrayIndependence() {
    Move move = new Move(TileColour.RED, 0, 0, true);

    int[] array1 = move.getMove();
    int[] array2 = move.getMove();

    // Should be different array instances
    assertNotSame(array1, array2);

    // But same values
    assertArrayEquals(array1, array2);

    // Modifying one shouldn't affect the other
    array1[0] = 999;
    assertEquals(0, array2[0]);
}
```

---

#### TC-M-009: testNegativeRowColumn
**Purpose:** Verify negative row/column are allowed

**Test Code:**
```java
@Test
void testNegativeRowColumn() {
    Move move = new Move(TileColour.RED, -1, -1, false);

    assertEquals(-1, move.getMove()[0]);
    assertEquals(-1, move.getMove()[1]);
}
```

---

#### TC-M-010: testLargeRowColumn
**Purpose:** Verify large row/column values are allowed

**Test Code:**
```java
@Test
void testLargeRowColumn() {
    Move move = new Move(TileColour.GOLD, 100, 100, true);

    assertEquals(100, move.getMove()[0]);
    assertEquals(100, move.getMove()[1]);
}
```

---

## Enum Tests

Total Tests: 10

### TileColour Enum Tests (6 tests)

#### TC-TC-001: testTileColourValues
**Purpose:** Verify enum has correct values

**Test Code:**
```java
@Test
void testTileColourValues() {
    TileColour[] values = TileColour.values();

    assertEquals(2, values.length);
    assertEquals(TileColour.RED, values[0]);
    assertEquals(TileColour.GOLD, values[1]);
}
```

---

#### TC-TC-002: testTileColourValueOf
**Purpose:** Verify valueOf works

**Test Code:**
```java
@Test
void testTileColourValueOf() {
    assertEquals(TileColour.RED, TileColour.valueOf("RED"));
    assertEquals(TileColour.GOLD, TileColour.valueOf("GOLD"));
}
```

---

#### TC-TC-003: testTileColourOrdinal
**Purpose:** Verify ordinal values

**Test Code:**
```java
@Test
void testTileColourOrdinal() {
    assertEquals(0, TileColour.RED.ordinal());
    assertEquals(1, TileColour.GOLD.ordinal());
}
```

---

#### TC-TC-004: testTileColourComparison
**Purpose:** Verify enum comparison

**Test Code:**
```java
@Test
void testTileColourComparison() {
    assertNotEquals(TileColour.RED, TileColour.GOLD);
    assertEquals(TileColour.RED, TileColour.RED);
}
```

---

#### TC-TC-005: testTileColourInvalidValueOf
**Purpose:** Verify invalid valueOf throws exception

**Test Code:**
```java
@Test
void testTileColourInvalidValueOf() {
    assertThrows(IllegalArgumentException.class, () -> {
        TileColour.valueOf("BLUE");
    });
}
```

---

#### TC-TC-006: testTileColourNullValueOf
**Purpose:** Verify null valueOf throws exception

**Test Code:**
```java
@Test
void testTileColourNullValueOf() {
    assertThrows(NullPointerException.class, () -> {
        TileColour.valueOf(null);
    });
}
```

---

### GameState Enum Tests (4 tests)

#### TC-GS-001: testGameStateValues
**Purpose:** Verify enum has correct values

**Test Code:**
```java
@Test
void testGameStateValues() {
    GameState[] values = GameState.values();

    assertEquals(3, values.length);
    assertEquals(GameState.IN_PROGRESS, values[0]);
    assertEquals(GameState.WIN, values[1]);
    assertEquals(GameState.DRAW, values[2]);
}
```

---

#### TC-GS-002: testGameStateValueOf
**Purpose:** Verify valueOf works

**Test Code:**
```java
@Test
void testGameStateValueOf() {
    assertEquals(GameState.IN_PROGRESS, GameState.valueOf("IN_PROGRESS"));
    assertEquals(GameState.WIN, GameState.valueOf("WIN"));
    assertEquals(GameState.DRAW, GameState.valueOf("DRAW"));
}
```

---

#### TC-GS-003: testGameStateOrdinal
**Purpose:** Verify ordinal values

**Test Code:**
```java
@Test
void testGameStateOrdinal() {
    assertEquals(0, GameState.IN_PROGRESS.ordinal());
    assertEquals(1, GameState.WIN.ordinal());
    assertEquals(2, GameState.DRAW.ordinal());
}
```

---

#### TC-GS-004: testGameStateInvalidValueOf
**Purpose:** Verify invalid valueOf throws exception

**Test Code:**
```java
@Test
void testGameStateInvalidValueOf() {
    assertThrows(IllegalArgumentException.class, () -> {
        GameState.valueOf("INVALID");
    });
}
```

---

## Integration Tests

Total Tests: 8+

#### TC-INT-001: testFullGameHorizontalWin
**Purpose:** Complete game ending in horizontal win

**Test Code:**
```java
@Test
void testFullGameHorizontalWin() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // P1 builds horizontal win in bottom row
    game.makeMove(p1, 0); // RED [5,0]
    game.makeMove(p2, 0); // GOLD [4,0]
    game.makeMove(p1, 1); // RED [5,1]
    game.makeMove(p2, 1); // GOLD [4,1]
    game.makeMove(p1, 2); // RED [5,2]
    game.makeMove(p2, 2); // GOLD [4,2]
    String result = game.makeMove(p1, 3); // RED [5,3] - WIN

    assertTrue(result.contains("Winner is 1"));

    // No more moves allowed
    String blocked = game.makeMove(p2, 4);
    assertEquals("Game is no longer ACTIVE", blocked);
}
```

---

#### TC-INT-002: testFullGameVerticalWin
**Purpose:** Complete game ending in vertical win

---

#### TC-INT-003: testFullGameDiagonalWinAscending
**Purpose:** Complete game ending in ascending diagonal win

---

#### TC-INT-004: testFullGameDiagonalWinDescending
**Purpose:** Complete game ending in descending diagonal win

---

#### TC-INT-005: testFullGameDraw
**Purpose:** Complete game ending in draw

---

#### TC-INT-006: testFullGameWithInvalidMoves
**Purpose:** Game with several invalid move attempts

**Test Code:**
```java
@Test
void testFullGameWithInvalidMoves() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // Valid move
    game.makeMove(p1, 0);

    // Invalid: out of turn
    String r1 = game.makeMove(p1, 1);
    assertEquals("Player cannot make a move.", r1);

    // Valid move
    game.makeMove(p2, 1);

    // Invalid: out of turn
    String r2 = game.makeMove(p2, 2);
    assertEquals("Player cannot make a move.", r2);

    // Game should still be playable
    String r3 = game.makeMove(p1, 2);
    assertEquals("Move was valid", r3);
}
```

---

#### TC-INT-007: testFullGameWinOnLastMove
**Purpose:** Game where final move (42nd) creates win

---

#### TC-INT-008: testFullGameStateTransitions
**Purpose:** Verify state transitions throughout game

**Test Code:**
```java
@Test
void testFullGameStateTransitions() {
    Player p1 = new Player(1, TileColour.RED);
    Player p2 = new Player(2, TileColour.GOLD);
    GameEngine game = new GameEngine(p1, p2);

    // IN_PROGRESS state
    String r1 = game.makeMove(p1, 0);
    assertFalse(r1.contains("Winner"));
    assertFalse(r1.contains("Draw"));

    // Still IN_PROGRESS
    String r2 = game.makeMove(p2, 1);
    assertFalse(r2.contains("Winner"));

    // Create win
    game.makeMove(p1, 0);
    game.makeMove(p2, 1);
    game.makeMove(p1, 0);
    game.makeMove(p2, 2);
    String winResult = game.makeMove(p1, 0);

    // WIN state
    assertTrue(winResult.contains("Winner"));

    // Game ended
    String afterWin = game.makeMove(p2, 3);
    assertEquals("Game is no longer ACTIVE", afterWin);
}
```

---

## Test Execution Summary

### Total Test Count: 100+

**By Class:**
- Board: 38 tests
- GameEngine: 19 tests
- Player: 9 tests
- Move: 10 tests
- TileColour: 6 tests
- GameState: 4 tests
- Integration: 8 tests

**By Priority:**
1. **High Priority** (Fix bugs first, then test):
   - Board.getTileCount() direction mutation
   - Board input validation
   - Player.equals() implementation
   - Board parameterized constructor

2. **Medium Priority** (Core functionality):
   - Board win detection (all directions)
   - GameEngine turn management
   - GameEngine win/draw detection

3. **Low Priority** (Edge cases):
   - Invalid inputs
   - Boundary conditions
   - Null handling

### Running Tests

**Run all tests:**
```bash
mvn test
```

**Run specific test class:**
```bash
mvn test -Dtest=BoardTest
```

**Run specific test method:**
```bash
mvn test -Dtest=BoardTest#testCheckWinHorizontalBottomRow
```

**Generate test coverage report:**
```bash
mvn jacoco:prepare-agent test jacoco:report
```

---

## Test Writing Guidelines

1. **Naming Convention:**
   - test[MethodName][Scenario]
   - Example: testCheckWinHorizontalBottomRow

2. **Structure (AAA Pattern):**
   - **Arrange:** Set up test data
   - **Act:** Execute the method under test
   - **Assert:** Verify the results

3. **One Assertion Per Test (when possible):**
   - Makes failures easier to diagnose
   - Exceptions: Related assertions (e.g., array contents)

4. **Use @DisplayName for clarity:**
   ```java
   @Test
   @DisplayName("Should detect horizontal win in bottom row")
   void testCheckWinHorizontalBottomRow() { ... }
   ```

5. **Test Independence:**
   - Each test should be independent
   - Use @BeforeEach for common setup
   - Don't rely on test execution order

6. **Test Data:**
   - Use meaningful test values
   - Avoid magic numbers
   - Consider edge cases (0, -1, MAX_VALUE, null)

---

## Next Steps

1. Fix the 4 critical bugs identified
2. Add getter methods to GameEngine for better testability
3. Write tests in this order:
   - Board basic functionality
   - Board win detection
   - GameEngine turn management
   - GameEngine win/draw detection
   - Player and Move classes
   - Integration tests
4. Run tests frequently during development
5. Aim for 80%+ code coverage
6. Fix any failing tests immediately

---

**Document Version:** 1.0
**Last Updated:** 2025-12-16
**Total Test Cases:** 100+

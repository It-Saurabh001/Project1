# 🔍 COMPREHENSIVE CODE REVIEW PROMPT: Sudoku Modern Android Application

## MASTER INSTRUCTION

You are an expert Android architect, security engineer, performance specialist, and UX designer. Your task is to conduct a **thorough, production-grade code review** of the Sudoku Modern Android Application, analyzing every aspect from architectural integrity to user experience across device form factors.

---

## PART 1: FUNCTIONALITY & FEATURE COMPLETENESS REVIEW

### 1.1 Core Game Logic Validation

**Review the following game mechanics:**

- [ ] **Puzzle Generation Algorithm**
    - Is the backtracking puzzle generation truly random and unbiased?
    - Does `SudokuGenerator.fillBoard()` correctly shuffle arrays before placement?
    - Are there any edge cases where `hasUniqueSolution()` could miss duplicate solutions?
    - Verify all 81 cells are correctly iterated in random order during clue removal
    - Check if `cellsToRemove` calculation matches target clue counts (EASY: 40, MEDIUM: 35, HARD: 28, EXPERT: 22)
    - Are there any off-by-one errors in clue validation?
    - Performance: Does generation complete in < 2 seconds for all difficulties?

- [ ] **Move Validation & Conflict Detection**
    - `ValidateMoveUseCase`: Does it correctly compare placed number against `board.solution[row][col]`?
    - Are initial clue cells properly protected from overwriting?
    - Does `GameUtils.isBoardComplete()` correctly validate all 81 cells are non-zero AND valid?
    - Conflict detection: Does `getConflictingCells()` correctly identify:
        - Row conflicts (all cells in same row with same value)
        - Column conflicts (all cells in same column with same value)
        - 3x3 box conflicts (all cells in same 3x3 box with same value)
    - Are conflicts correctly identified when the solution value is placed (should NOT show as conflict)?
    - Can you place the same number twice in a row and both show as incorrect?

- [ ] **Mistake Counting & Game Over Logic**
    - Mistake limit per difficulty: EASY (5), MEDIUM (3), HARD (2), EXPERT (1)
    - Does `mistakes >= maxMistakes` correctly trigger `GameState.LOST`?
    - Are mistakes only incremented for incorrect placements, not for erasing or notes?
    - If a player places wrong number, does it:
        - Increment mistake counter ✓
        - Show in red color ✓
        - Play error sound ✓
        - Trigger shake animation ✓
        - Allow undo to restore state ✓
    - Can player place an incorrect number, then undo it, without it counting against them?
    - If player fixes an incorrect number, does mistake count remain as-is?

- [ ] **Undo / Redo Functionality**
    - Does `moveHistory` capture:
        - Cell position (row, col) ✓
        - Old value ✓
        - New value ✓
        - Complete notes snapshot before & after? ✓
    - When undoing: Are both numbers AND candidate marks restored correctly?
    - When redoing: Does the exact state get restored (including notes)?
    - Can player undo all moves to return to initial puzzle state?
    - Can player redo all undone moves to return to game state?
    - Are undo/redo history limits set (prevent memory bloat)?
    - What happens if player makes a move while in redo state? (Should clear redo stack)

- [ ] **Notes / Pencil Marks System**
    - `Notes` class maintains 9x9 array of `MutableSet<Int>` (1-9 candidates)
    - Can user toggle individual candidates on/off in notes mode?
    - Does `autoRemoveNotes()` correctly prune candidates when number is placed?
        - Prune from entire row ✓
        - Prune from entire column ✓
        - Prune from entire 3x3 box ✓
    - Example: If user places 5 in cell (0,0), does 5 get removed from all notes in row 0, column 0, and top-left 3x3 box?
    - If user undoes a number placement, are pruned candidates restored?
    - Can notes be cleared individually, or only all-at-once?
    - Are notes properly serialized/deserialized from JSON in database?

- [ ] **Hint System**
    - `GetHintUseCase`: Does it find random empty cell and return solution value?
    - Max hints per game: Set to 3 (verify in `Constants.MAX_HINTS`)
    - Does hint badge display remaining hints accurately?
    - When all hints used, is hint button disabled?
    - Does using a hint:
        - Populate the cell with correct number ✓
        - Play hint audio ✓
        - NOT increment mistakes ✓
        - Update `hintsUsed` counter ✓
        - Trigger animation ✓
    - Are hints trackable for achievements ("Mind Reader" - 0 hints)?

- [ ] **Game Timer & Lifecycle-Aware Pausing**
    - Timer increments every 1,000 ms in `GameViewModel`
    - When app goes to background (PAUSED), does timer pause?
    - When app returns to foreground (RESUMED), does timer resume correctly?
    - If game state = `PAUSED`, does timer not tick?
    - Time calculation: Is elapsed time = `currentTime - startTime` in milliseconds?
    - Format display: Is time displayed as `MM:SS` correctly (e.g., 5:32 = 5 minutes 32 seconds)?
    - Does timer correctly handle games played across app restarts?
    - If app crashes, is elapsed time preserved by `completedAt - startTime`?

- [ ] **Game Completion & Statistics Recording**
    - When board is complete & valid, does `CheckGameCompletionUseCase` trigger?
    - Does completion record:
        - Game state = `COMPLETED` ✓
        - `completedAt` timestamp ✓
        - Final mistakes count ✓
        - Total elapsed time ✓
        - Hints used ✓
    - Statistics update includes:
        - `gamesCompleted` += 1 ✓
        - `totalGamesPlayed` += 1 ✓
        - Difficulty-specific `bestTime` (only if `gameTime < existing bestTime`) ✓
        - `totalPlayTime` += elapsed time ✓
        - `currentStreak` += 1 ✓
        - `longestStreak` = max(`longestStreak`, `currentStreak`) ✓
        - `lastPlayedDate` = today's date ✓
    - Are lost games correctly recorded in statistics?
        - `gamesLost` += 1 ✓
        - `totalGamesPlayed` += 1 ✓
        - `currentStreak` = 0 ✓

---

### 1.2 Feature Parity & Completeness

- [ ] **Home Screen**
    - Does "Continue Game" card appear if there's an active game?
    - Does "Continue Game" show:
        - Game preview (partial board state) ✓
        - Difficulty badge (EASY, MEDIUM, HARD, EXPERT) ✓
        - Elapsed time so far ✓
        - Mistakes counter ✓
    - "New Game" section displays difficulty selector with accurate clue counts?
    - Are quick stats visible (total wins, win rate, average time)?

- [ ] **Game Screen**
    - Board renders correctly (9x9 grid with 3x3 box separators)?
    - Toolbar displays: Timer, Mistakes, Notes Toggle, Hints, Undo, Redo, Pause?
    - NumberPad shows 1-9 buttons with remaining count badges?
    - When 9 of a number are placed, is that button disabled?
    - Notes mode toggle visually indicates active state?
    - Selected cell is highlighted?
    - Related cells (same row, column, box, same number) are highlighted?
    - Conflicts are highlighted in red?
    - Initial clue cells are visually distinct (unclickable)?

- [ ] **Statistics Screen**
    - Overall stats display:
        - Total games played ✓
        - Win/loss counts ✓
        - Win rate percentage ✓
        - Total play time formatted as `HH:MM:SS` ✓
        - Average game time ✓
        - Total hints used ✓
    - Best times per difficulty visible?
    - Win streak information (current & longest)?
    - Difficulty-specific metrics available?

- [ ] **Settings Screen**
    - Theme selector (Light, Dark, System)
    - Sound toggle
    - Haptic feedback toggle
    - Auto-notes toggle
    - Show hints toggle
    - Error highlighting toggle
    - Auto-save toggle
    - Notes mode default toggle
    - Clear cache / reset data option?

- [ ] **Trophy Room**
    - All 10 achievements visible?
    - Locked achievements show progress (e.g., "Win 2/5 games in a row")?
    - Unlock animation when achievement is earned?
    - Achievement description & emoji visible?
    - Achievements sorted by unlock status (unlocked first)?
    - Confetti animation on unlock?

---

## PART 2: SECURITY ANALYSIS

### 2.1 Data Storage & Encryption

- [ ] **Room Database Security**
    - Is database encrypted at rest?
        - [ ] Implement SQLCipher encryption
        - [ ] Use `SupportSQLiteOpenHelper.Configuration.Builder()` with passphrase
        - [ ] Derive passphrase from device keystore, not hardcoded
    - Are sensitive columns encrypted (board state, solution)?
    - Database file permissions: Is it readable only by app (`MODE_PRIVATE`)?
    - Verify database file location: `context.getDatabasePath()` returns app-specific directory
    - Are migrations tested to ensure encrypted DB can upgrade?
    - Is database backed up or excluded from backup?
        - [ ] Add `android:allowBackup="false"` or exclude via `BackupAgent`

- [ ] **DataStore Preferences Security**
    - Are user preferences encrypted?
        - [ ] Implement `EncryptedSharedPreferences` or encrypted DataStore
    - Sensitive data in preferences:
        - Theme preference ✓ (non-sensitive)
        - Sound/haptic settings ✓ (non-sensitive)
    - Are preferences readable by other apps? (Verify file permissions)

- [ ] **In-Memory State Security**
    - Is board/solution matrix held in memory unencrypted? (Necessary for game logic)
    - Are sensitive strings (API keys, seeds) in memory for minimal duration?
    - Are views/bitmaps containing sensitive info cleared on app pause?
    - Does Compose leak recomposition details?

---

### 2.2 Input Validation & Injection Prevention

- [ ] **User Input in Game Moves**
    - Number input (1-9): Is range validated?
        - [ ] Enforce `number in 1..9`
    - Row/Column input (0-8): Is range validated?
        - [ ] Enforce `row in 0..8 && col in 0..8`
    - Can user input negative numbers, 10+, or non-integers?
    - Is user input sanitized before database persistence?

- [ ] **JSON Deserialization (Gson)**
    - Board/solution matrices deserialized from JSON in database
    - Can malformed JSON crash the app?
    - Are exception handlers in place?
        - [ ] Wrap `Gson.fromJson()` in try-catch
        - [ ] Log errors and provide fallback (regenerate puzzle or show error)
    - Can oversized or deeply nested JSON cause OutOfMemory?
    - Verify Gson configuration: `excludeFieldsWithoutExposeAnnotation()` or `serializeNulls()`?

---

### 2.3 Memory & Overflow Vulnerabilities

- [ ] **Array Bounds & Off-by-One Errors**
    - 9x9 board matrices:
        - [ ] Verify all iterations: `for (row in 0..8)` not `0..9`
        - [ ] Check cell access: `board[row][col]` not `board[row+1][col+1]`
    - 3x3 box indexing: Calculate box start as `(row / 3) * 3` and `(col / 3) * 3`
        - [ ] Does loop correctly iterate 9 cells in each 3x3 box?
    - Candidate set operations: `MutableSet<Int>` with numbers 1-9
        - [ ] Verify no negative or >9 numbers added
    - MoveHistory list: Are move indices within bounds?
        - [ ] Prevent `moveIndex` from exceeding `moveHistory.size - 1`

- [ ] **Memory Leaks & Unbounded Collections**
    - `moveHistory` (Undo/Redo stack):
        - [ ] Set maximum size (e.g., 100 moves max)
        - [ ] Clear redo stack on new move
    - `StateFlow<GameUiState>`:
        - [ ] Is `viewModelScope` correctly used?
        - [ ] Do Flows unsubscribe when ViewModel is cleared?
    - Bitmaps / Canvas in Compose:
        - [ ] Are large bitmaps released in `onDispose {}`?
    - Timer coroutine:
        - [ ] Does timer job cancel in `onCleared()`?

- [ ] **Stack Overflow in Recursion**
    - `SudokuGenerator.fillBoard()` uses recursive backtracking
        - [ ] What's the maximum recursion depth? (Should be ~81)
        - [ ] Will stack overflow occur on deeply nested recursion?
        - [ ] Consider iterative approach with explicit stack for deep puzzles
    - `SudokuSolver` backtracking:
        - [ ] Same recursion depth concern
        - [ ] Early exit when 2nd solution found

---

### 2.4 Authentication & Authorization

- [ ] **Local User Sessions**
    - Is app accessed without authentication? (Expected for offline game)
    - Are game save files associated with user profile?
    - Multi-user device: Can one user see another user's games?
        - [ ] Implement per-user database separation or encryption key derivation
    - Guest accounts: Supported?

- [ ] **Data Isolation**
    - Are user statistics isolated to their profile?
    - Can one user access another user's game records?
    - Verify `Context.getFilesDir()` and `Context.getDatabasePath()` are user-scoped

---

### 2.5 Code Injection & Malicious Input

- [ ] **SQL Injection (Room DAOs)**
    - All Room queries are parameterized (`@Query` with `?` placeholders)?
    - No string concatenation in SQL queries?
    - Example safe: `@Query("SELECT * FROM games WHERE id = ?1")`
    - Example unsafe: `@Query("SELECT * FROM games WHERE id = '$gameId'")`

- [ ] **Serialization Vulnerabilities (Gson)**
    - Custom deserializers implemented safely?
    - Are public constructors avoided in models to prevent reflection abuse?
    - Verify no `TypeToken` wildcards that could deserialize arbitrary objects

- [ ] **Intent & URI Handling**
    - Does app accept deep links or custom schemes?
    - Are intent extras validated?
    - URI path traversal: Can malicious URI access files outside app directory?

---

### 2.6 Crash Reporting & Error Handling

- [ ] **Unhandled Exceptions**
    - Are all try-catch blocks in place for:
        - Database operations ✓
        - JSON deserialization ✓
        - File I/O ✓
        - Coroutine execution ✓
    - Do exceptions trigger user-friendly error messages?
    - Are exceptions logged with full stack trace for debugging?

- [ ] **Crash Recovery**
    - If app crashes during game, can player resume?
    - Is game state persisted frequently (after each move)?
    - Can corrupted database be recovered or reset?

---

## PART 3: DATA FLOW & STATE MANAGEMENT ANALYSIS

### 3.1 Unidirectional Data Flow (UDF) Verification

**Trace complete data flow from user interaction to database and back:**

```
┌──────────────────────────────────────────────────────────────┐
│  USER INTERACTION: Taps cell (row, col)                      │
├──────────────────────────────────────────────────────────────┤
│  COMPOSABLE: SudokuCell ──(onCellSelected(row, col))────→    │
│                                                               │
│  VIEWMODEL: GameViewModel.selectCell(row, col)               │
│    ├─ Update selectedCell in uiState                         │
│    └─ Recompute highlights, conflicts                        │
│                                                               │
│  REPOSITORY: (No change yet, awaiting number input)           │
│                                                               │
│  UI: StateFlow<GameUiState> emits update                     │
│    ├─ Highlight selected cell                               │
│    ├─ Highlight related cells                               │
│    └─ Update board display                                  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  USER INTERACTION: Taps number 5 on keypad                   │
├──────────────────────────────────────────────────────────────┤
│  COMPOSABLE: NumberPad ──(onNumberSelected(5))────→          │
│                                                               │
│  VIEWMODEL: GameViewModel.placeNumber(5)                     │
│    ├─ Check if cell is selected                              │
│    ├─ Check if in notes mode (toggle candidate vs place)     │
│    ├─ Call ValidateMoveUseCase.validate()                    │
│    │   ├─ Check against solution[row][col]                  │
│    │   └─ Return (isValid, newMistakes)                      │
│    │                                                          │
│    ├─ If correct:                                            │
│    │   ├─ Place on board[row][col]                          │
│    │   ├─ Call autoRemoveNotes() for row/col/box            │
│    │   ├─ Add move to moveHistory (undo stack)              │
│    │   ├─ Play correct audio                                │
│    │   ├─ Trigger scale animation                           │
│    │   ├─ Update numberPad counts                           │
│    │   ├─ Check if board complete                           │
│    │   └─ Call CheckGameCompletionUseCase if complete       │
│    │                                                          │
│    ├─ If incorrect:                                          │
│    │   ├─ Place on board in red (user made number)          │
│    │   ├─ Increment mistakes counter                        │
│    │   ├─ Add move to moveHistory                           │
│    │   ├─ Play mistake audio                                │
│    │   ├─ Trigger shake animation                           │
│    │   ├─ Check if game over (mistakes >= max)              │
│    │   └─ If game over: Record loss in statistics           │
│    │                                                          │
│    └─ Call SaveGameUseCase.save(updatedGame)                 │
│                                                               │
│  REPOSITORY: GameRepositoryImpl.saveGame()                    │
│    └─ Serialize board, solution, notes to JSON              │
│    └─ gameDao.update(gameEntity)                            │
│                                                               │
│  DATABASE: Room DAOs                                         │
│    ├─ Execute SQL UPDATE games SET ...                      │
│    └─ Database triggers StateFlow emission                  │
│                                                               │
│  REPOSITORY: Emits updated Game via Flow                     │
│                                                               │
│  VIEWMODEL: Collects updated Game from repository Flow       │
│    └─ Merges with move history and local state              │
│    └─ Recomputes conflicts, highlights                      │
│    └─ Emits updated GameUiState                             │
│                                                               │
│  UI: StateFlow<GameUiState> triggers recomposition           │
│    ├─ Render updated board                                  │
│    ├─ Show updated mistake counter                          │
│    ├─ Update numberPad availability                         │
│    └─ Display animations                                    │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 State Consistency Checks

- [ ] **Single Source of Truth (SSOT)**
    - Is Room database the SSOT for game state?
    - Does every state change flow through the repository layer?
    - Can local ViewModel state diverge from database?
    - Example: If database is updated externally (should not happen in offline app), does ViewModel sync?

- [ ] **State Synchronization**
    - When game is loaded, does ViewModel collect latest from repository?
    - When move is made, is it immediately saved to database?
    - Delay between user action and database save: < 100ms?
    - If app crashes mid-move, is game state recoverable?

- [ ] **Race Conditions**
    - Can user make multiple moves simultaneously?
    - Does ViewModel queue moves or discard duplicates?
    - Multiple coroutines accessing `moveHistory`: Is it thread-safe?
    - Database: Are SQL updates atomic (single transaction)?

---

### 3.3 Data Flow Anomalies & Edge Cases

- [ ] **Flow Cancellation & Memory Leaks**
    - Are all Flows collected with `lifecycleScope.launch()`?
    - Do Flows unsubscribe when Fragment is destroyed?
    - Is `collectAsStateWithLifecycle()` used to respect lifecycle?
    - Can Composables cause Flow leaks if recomposed frequently?

- [ ] **Stale State Issues**
    - If user resumes app after 10 minutes, is timer time accurate?
    - If game is saved, closed, and reopened, does game load with correct state?
    - If user makes 50 moves, undoes 30, then makes 5 more moves:
        - Are redo moves correctly discarded?
        - Is move history consistent?

- [ ] **Concurrent Updates**
    - If multiple coroutines call `placeNumber()` simultaneously, is state corrupted?
    - Timer and move placement: Can they collide?
    - Database writes: Are they serialized (one-at-a-time)?

---

## PART 4: ANIMATION & VISUAL FEEDBACK ANALYSIS

### 4.1 Animation Smoothness & Performance

- [ ] **Cell Placement Animations**
    - Scale pop animation on correct placement:
        - Start scale: 0.8x → End scale: 1.0x
        - Duration: 150-300ms?
        - Easing: `EaseOut` or `Spring`?
        - Target: 60 fps (no janky animation)
    - Does animation interrupt if user makes multiple placements quickly?
    - Is animation GPU-accelerated (using `transformOrigin` and `scale`)?

- [ ] **Error Shake Animation**
    - Triggered on mistake placement
    - Amplitude: 10-15dp shake?
    - Duration: 300-400ms?
    - Direction: Horizontal or both?
    - Does shake prevent user interaction during animation?

- [ ] **Cell Highlight Animations**
    - Selected cell highlight:
        - Color transition smooth? (Use `animateColorAsState`)
        - Duration: 150-200ms?
    - Related cells highlight:
        - All row/column/box cells highlight in unison?
        - Color distinct from selection?
    - Conflict highlight (red):
        - Animation or instant switch?
        - Does it flash to grab attention?

- [ ] **Timer & Counter Animations**
    - Mistake counter updates:
        - Does it animate (pulse, scale, color change)?
        - Duration: 100-200ms?
    - Timer ticks:
        - Does number changing cause visual jank?
        - Smoother: Render time as single Text() or segment by segment?

- [ ] **Page Transition Animations**
    - Navigation from Home → Game Screen:
        - Fade-in, slide-up, or other?
        - Duration: 300-400ms?
    - Navigation from Game → Home:
        - Reverse animation?
    - Dialog appearances (Game Over, Victory):
        - Slide-up from bottom?
        - Fade-in?
        - Scrim (dark background) animated?

- [ ] **Victory Celebration Animation**
    - Triggered on board completion
    - Confetti or particle effect?
    - Duration: 2-3 seconds?
    - Sound (fanfare) synchronized with animation?
    - Does dialog prevent dismissal during animation?

---

### 4.2 Visual Consistency & Design System

- [ ] **Color Scheme (Material 3)**
    - Light mode colors defined:
        - Primary, Secondary, Tertiary?
        - Background, Surface colors?
        - Error color (red for mistakes)?
    - Dark mode colors defined:
        - All light mode colors have dark equivalents?
        - Sufficient contrast (WCAG AA: 4.5:1 for text)?
    - Dynamic color (Material You): Implemented?
        - [ ] Use `ColorScheme.from(image)` on Android 12+
        - [ ] Fallback to static colors on older versions

- [ ] **Typography Scale**
    - Heading: 28sp bold?
    - Body: 16sp regular?
    - Label (buttons, badges): 14sp medium?
    - Timer text: 32sp bold and readable?
    - Board cell text: 20-24sp bold and centered?

- [ ] **Spacing & Layout**
    - Padding consistency: 4dp, 8dp, 16dp, 24dp grid?
    - Cell size: Aspect ratio 1:1 (perfect square)?
    - 3x3 box separators: Thickness 2-3dp?
    - Margin between rows/columns: Consistent?

- [ ] **Status Bar & System UI**
    - Status bar color matches theme?
    - Navigation bar color matches theme?
    - Edge-to-edge design: Implemented?
        - [ ] `enableEdgeToEdge()` in MainActivity
    - System icons adapt to background brightness?

---

### 4.3 Accessibility for Animations

- [ ] **Reduced Motion Preference**
    - Does app respect `android.view.accessibility.disableAnimations`?
    - Can animations be disabled in settings?
    - All interactive elements functional without animation?

- [ ] **Visual Indicators Without Animation**
    - Can selected cell be identified by outline/border, not just color change?
    - Error states indicated by icon or shape, not just red color?
    - Can colorblind users distinguish cell states (use patterns, not color alone)?

---

## PART 5: DEVICE FORM FACTOR OPTIMIZATION

### 5.1 Mobile Phone (360dp - 480dp width)

- [ ] **Screen Adaptation**
    - 9x9 board fits within screen width with padding?
    - Board size calculation: `screenWidth - 32.dp` (16dp padding each side)?
    - Single cell size: `(screenWidth - 32.dp) / 9` ≈ 35-40dp on 360dp phone?
    - Are cells large enough to tap accurately (minimum 44dp per Material Design)?

- [ ] **Landscape Orientation**
    - Does app support landscape layout?
    - Is board still playable in landscape (width doubled)?
    - Keyboard (NumberPad) positioned optimally:
        - Bottom of screen on portrait? ✓
        - Right or left side on landscape?
    - Are toolbar controls accessible in landscape?

- [ ] **Notch & Cutout Handling**
    - Does EdgeToEdge handle notches properly?
    - Are important UI elements shifted below status bar?
    - Toolbar with timer: Is it readable with notch?

- [ ] **Touch Target Size**
    - NumberPad buttons: At least 44dp × 44dp?
    - Cell tap area: 35-40dp (may be below 44dp, is it usable)?
    - Action buttons (Undo, Redo, Hint): 48dp × 48dp?
    - Are touch targets visually distinct from safe areas?

- [ ] **Text Readability**
    - Body text: Minimum 14sp on small screens?
    - Timer text: 24-28sp (large and readable)?
    - Cell numbers: 18-20sp and bold?
    - Can user zoom text if needed?

---

### 5.2 Tablet (600dp - 1200dp+ width)

- [ ] **Layout Expansion**
    - Does app use multi-column layout on tablet?
    - Example: Board on left (400dp), stats/controls on right (300dp)?
    - Or full-width board with controls below?
    - Verify unused space is not wasted (symmetrical padding)?

- [ ] **Board Scaling**
    - Max board size on tablet: 600dp × 600dp?
    - Cells become larger: 67dp × 67dp (excellent tap targets)?
    - Text (28-32sp) scales with cell size, remaining readable?
    - Are 3x3 box borders still visible?

- [ ] **Master-Detail Navigation**
    - Home screen: Game list on left, quick stats on right?
    - Game screen: Board on left, notes/hints/undo on right?
    - Statistics screen: Category list on left, charts on right?

- [ ] **Landscape Optimization**
    - Tablet rotates to landscape (1200dp wide × 600dp tall)?
    - Can board be displayed at full resolution?
    - Toolbar controls: Horizontal or vertical alignment?
    - NumberPad: Compact layout (e.g., 3x3 instead of 1x9)?

- [ ] **Orientation Lock**
    - Default to user preference (system setting)?
    - Or allow rotation to landscape for board expansion?
    - Game state preserved across rotation (no restart)?

---

### 5.3 Adaptive Design Principles

- [ ] **Configuration Changes**
    - Does app handle configuration changes (rotation, language)?
    - Is ViewModel retained across configuration changes?
    - Are Flows reestablished correctly after rotation?
    - Game state not lost on device rotation?

- [ ] **Dynamic Layouts**
    - Use `WindowSizeClass` or `isLandscape()` to determine layout?
    - Example: `val isBigScreen = LocalConfiguration.current.screenWidthDp > 600`
    - Conditional rendering: `if (isBigScreen) { LandscapeLayout() } else { PortraitLayout() }`

- [ ] **Responsive Typography**
    - Font size scales based on screen width?
    - Example: `val baseFontSize = if (isBigScreen) 18.sp else 16.sp`
    - Or use Material 3 responsive type scale?

- [ ] **Touch Feedback**
    - Ripple effects on all interactive elements?
    - Haptic feedback on button press?
    - Visual indication of button pressed state?

---

## PART 6: THEME & VISUAL DESIGN REVIEW

### 6.1 Material 3 Implementation

- [ ] **Color System**
    - Primary color: App brand color (e.g., blue, purple)?
    - Secondary color: Complementary color for accents?
    - Tertiary color: Alternative accent (e.g., teal)?
    - All colors from Material 3 color system?

- [ ] **Light & Dark Modes**
    - Light mode background: White or near-white (#FFFBFE)?
    - Dark mode background: Dark surface (#1C1B1F)?
    - Text on light: Dark gray or black?
    - Text on dark: Light gray or white?
    - Sufficient contrast: WCAG AA or AAA?

- [ ] **Dynamic Color (Material You)**
    - On Android 12+, extract colors from wallpaper?
    - Fallback to static colors on Android 11 and below?
    - User can override dynamic colors in settings?

- [ ] **Shape & Elevation**
    - Cards: Rounded corners (12-16dp)?
    - Buttons: Rounded corners matching Material 3?
    - Shadows: Subtle elevation (2-4dp)?
    - Dark mode: Elevation less pronounced?

- [ ] **States & Interactions**
    - Disabled elements: Reduced opacity (38% alpha)?
    - Hover state: Overlay color change?
    - Pressed state: Color deepens?
    - Focus state: Outline or border?

---

### 6.2 Theme Switching

- [ ] **Theme Options**
    - Light theme
    - Dark theme
    - System theme (follow device settings)
    - Automatic switch at sunset/sunrise?

- [ ] **Dynamic Color Enabled**
    - On Android 12+: Use `dynamicLightColorScheme()` and `dynamicDarkColorScheme()`?
    - On older versions: Use static Material 3 color schemes?

- [ ] **Theme Persistence**
    - Selected theme saved in DataStore?
    - Persists across app restarts?
    - Does theme apply immediately without restart?

- [ ] **Screen-Specific Themes**
    - Can different screens have different themes?
    - Example: Dark background for game board (reduces eye strain)?
    - Overall app follows system theme, but game screen is always dark?

---

### 6.3 Branding & Visual Identity

- [ ] **App Icon**
    - Adaptive icon (background + foreground) defined?
    - Icon works on monochrome, rounded, teardrop, rounded square shapes?
    - Icon recognizable and distinct?

- [ ] **Splash Screen**
    - Modern splash screen using `SplashScreen` API?
    - Brand animated while app loads?
    - Smooth transition from splash to home screen?

- [ ] **Typography Branding**
    - Consistent typography across all screens?
    - Headlines use bold weight?
    - Body text uses regular weight?
    - Mono-space font for numbers (timer, scores)?

---

## PART 7: AREAS OF IMPROVEMENT

### 7.1 Performance Optimization

- [ ] **Recomposition Analysis**
    - GameScreen recomposes on every state emission?
    - Can you optimize using `remember` and `derivedStateOf`?
    - Are expensive operations memoized?
    - Profile with Compose Layout Inspector: Any red recompositions?

- [ ] **Database Query Optimization**
    - Are queries indexed properly?
    - Do queries use appropriate WHERE clauses?
    - Are JOINs used instead of multiple queries?
    - Background threads used for all database operations?

- [ ] **Memory Management**
    - Profiler: Max memory usage during gameplay?
    - Are bitmaps released after use?
    - Does memory spike during puzzle generation?
    - Is GC pressure acceptable (< 10 collections/minute)?

- [ ] **Startup Time**
    - Time to reach Home screen: < 2 seconds?
    - Time to reach Game screen: < 3 seconds?
    - Puzzle generation time: < 2 seconds per difficulty?

- [ ] **Battery Impact**
    - Timer update: Does wakelock prevent deep sleep?
    - Are coroutines cancelled to reduce CPU usage?
    - Sound/haptics drain battery? (Consider disable option)
    - How long can game be played on 1% battery?

---

### 7.2 User Experience Improvements

- [ ] **Onboarding**
    - Is first-time user experience smooth?
    - Are game rules explained (if needed)?
    - Is controls tutorial shown on first game?
    - Can tutorial be skipped?

- [ ] **Gesture Support**
    - Swipe to undo?
    - Long-press on cell for context menu?
    - Swipe left/right to navigate screens?
    - Double-tap for quick actions?

- [ ] **Offline Support**
    - App works without internet? ✓
    - Does app attempt network calls unnecessarily?
    - Error handling when network is unavailable?

- [ ] **Game Customization**
    - Can difficulty be chosen per game?
    - Can game mode be selected (classic, variations)?
    - Can blind mode be enabled (no cell numbers, notes only)?

---

### 7.3 Feature Enhancements

- [ ] **Multiplayer / Social**
    - Share puzzle with friends?
    - Leaderboards (local or cloud-synced)?
    - Daily puzzle challenges?

- [ ] **Advanced Features**
    - Puzzle timer speed modes (normal, fast, blitz)?
    - Puzzle variations (Jigsaw, Irregular, etc.)?
    - Custom difficulty selector (by clue count)?
    - Puzzle search/filter by difficulty and time?

- [ ] **Analytics**
    - Track game completion rates?
    - Analytics on user preferences?
    - Heatmap of cells users struggle with?

- [ ] **Cloud Backup**
    - Optional cloud sync of games and statistics?
    - Encrypted sync to user Google account?
    - Cross-device play?

---

### 7.4 Developer Experience Improvements

- [ ] **Logging & Debugging**
    - Debug logs for game state changes?
    - Can user export game state as JSON for analysis?
    - Performance profiling hooks?

- [ ] **Testing**
    - Unit tests for puzzle generation?
    - Unit tests for move validation?
    - Integration tests for game flow?
    - UI tests for critical paths?
    - Test coverage: Target 80%+?

- [ ] **Documentation**
    - Architecture decision records (ADR)?
    - Algorithm explanation for generation & solving?
    - Database schema documentation?
    - API / public interface documentation?

---

## PART 8: SPECIFIC CODE REVIEW POINTS

### 8.1 GameViewModel - Detailed Review

```kotlin
// Review Questions:
- Is moveHistory properly bounded (max size)?
- Does timer job cancel in onCleared()?
- Are all StateFlow emissions correct?
- Is there any mutable state outside StateFlow?
- Are coroutine launches using viewModelScope?
- Are exceptions caught and handled?
- Is database save called immediately after move?
- Are animations triggered in ViewModel or Composable?
```

### 8.2 Puzzle Generation Algorithm - Detailed Review

```kotlin
// Review Questions:
- Is randomization truly random (uses Kotlin Random)?
- Does fillBoard() correctly place all numbers?
- Is clue removal truly random (shuffled order)?
- Does hasUniqueSolution() correctly count to 2?
- What's the complexity? O(81 * N) where N = search space?
- Can puzzle generation get stuck in infinite loop?
- Are all edge cases handled (empty cells, etc.)?
- Is generated puzzle always valid?
```

### 8.3 Room Database - Detailed Review

```kotlin
// Review Questions:
- Are all migrations tested?
- Do queries handle NULL values correctly?
- Are SQL strings properly parameterized?
- Do DAOs use Flows or one-shot queries?
- Is database accessed on background threads?
- Are transactions used for atomic multi-table updates?
- Can database file be corrupted, causing crashes?
- Is database schema normalized?
```

### 8.4 Composable Structure - Detailed Review

```kotlin
// Review Questions:
- Are Composables receiving stable parameters?
- Are lambdas stable (use remember { } or @Composable)?
- Are Composables too large (> 200 lines)?
- Are State, StateFlow collected properly?
- Is recomposition efficient?
- Are Modifiers applied consistently?
- Are animations using proper Compose APIs?
```

---

## PART 9: SECURITY CHECKLIST (Comprehensive)

**Run through each item and verify implementation:**

- [ ] **Local Storage**
    - [ ] Room database encrypted (SQLCipher)
    - [ ] DataStore encrypted
    - [ ] SharedPreferences secure (deprecated, should use DataStore)
    - [ ] File permissions: `MODE_PRIVATE` for app-specific files
    - [ ] No sensitive data in Logcat

- [ ] **Memory Security**
    - [ ] Sensitive strings cleared after use
    - [ ] No hardcoded secrets (API keys, passwords)
    - [ ] Memory cleared on app pause
    - [ ] Bitmaps released to prevent leaks

- [ ] **Input Validation**
    - [ ] All user inputs validated
    - [ ] JSON deserialization exception-safe
    - [ ] No integer overflow in calculations
    - [ ] No array bounds violations

- [ ] **Data Integrity**
    - [ ] Database checksums or integrity checks
    - [ ] Game state not modifiable by external process
    - [ ] Saves are atomic (transactions)
    - [ ] No partial saves causing corruption

- [ ] **Process Security**
    - [ ] Debuggable flag false in release builds
    - [ ] No debug symbols in release APK
    - [ ] No test dependencies in release build
    - [ ] ProGuard/R8 obfuscation enabled

- [ ] **Platform Security**
    - [ ] Minimum SDK protects against old vulnerabilities
    - [ ] Target SDK is current (within 1 year)
    - [ ] Uses AndroidX libraries (not deprecated support libs)
    - [ ] No known CVEs in dependencies

---

## PART 10: PERFORMANCE METRICS & TARGETS

**Measure and benchmark the following:**

| Metric | Target | Measurement | Status |
|--------|--------|-------------|--------|
| App startup time | < 2s | Time from cold start to Home screen | TBD |
| Puzzle generation time | < 2s | Time from difficulty select to Game screen | TBD |
| Move validation latency | < 50ms | Time from number tap to board update | TBD |
| Undo/Redo latency | < 100ms | Time from undo tap to board restore | TBD |
| Game save latency | < 100ms | Time from move to database persistence | TBD |
| Memory at rest | < 100MB | RAM usage when idle on Home screen | TBD |
| Memory during game | < 150MB | RAM usage during active gameplay | TBD |
| Frame rate (animation) | 60 fps | Frame drops during cell animations | TBD |
| Battery drain per hour | < 5% | Battery consumption during 1 hour gameplay | TBD |
| Database size | < 50MB | Size of SQLite database after 1000 games | TBD |

---

## PART 11: TESTING & QUALITY ASSURANCE

### 11.1 Unit Tests Required

```kotlin
// Puzzle Generation
- testFillBoardGeneratesValidBoard()
- testGeneratedPuzzleHasUniqueSolution()
- testDifficultyTargetClueCount()

// Game Logic
- testPlaceCorrectNumber()
- testPlaceIncorrectNumberIncrementsMistakes()
- testMistakeLimitTriggersGameOver()
- testUndoRestoresState()
- testRedoRestoresState()
- testAutoRemoveNotesFromRow()
- testAutoRemoveNotesFromColumn()
- testAutoRemoveNotesFromBox()

// Statistics
- testGameWinRecordsStatistics()
- testBestTimeUpdatedOnlyIfFaster()
- testStreakIncrementedOnWin()
- testStreakResetOnLoss()

// Validation
- testBoardCompleteDetection()
- testConflictDetection()
- testHintReturnsValidNumber()
```

### 11.2 UI Tests Required

```kotlin
// Navigation
- testNavigateFromHomeToGame()
- testNavigateFromGameToHome()
- testNavigateToStatistics()
- testNavigateToSettings()

// Game Interaction
- testSelectCell()
- testPlaceNumber()
- testToggleNotesMode()
- testUseHint()
- testPauseGame()
- testResumeGame()

// Configuration Changes
- testGameStatePersistsAcrossRotation()
- testGameStateRestoresAfterAppRestart()
```

### 11.3 Integration Tests Required

```kotlin
// Database
- testGameSavedToDatabase()
- testGameLoadedFromDatabase()
- testStatisticsUpdatedCorrectly()

// Coroutines
- testMoveQueuedAndProcessedInOrder()
- testTimerContinuesAfterPause()
```

---

## PART 12: OUTPUT & DELIVERABLES

### Report Structure

**For each section reviewed, provide:**

1. **Summary**: High-level findings (✅ Pass, ⚠️ Warning, ❌ Fail)
2. **Detailed Findings**: Specific issues and locations
3. **Code Examples**: Show problematic code and fixes
4. **Risk Assessment**: Security, performance, or UX impact
5. **Recommendations**: Prioritized improvements (Critical, High, Medium, Low)
6. **Effort Estimate**: Hours to implement recommendations

### Critical Issues to Flag

**Must fix before release:**
- Security vulnerabilities (data exposure, injection, overflow)
- Crash-inducing bugs (null pointer, array bounds)
- Data corruption risks (loss of game state, statistics)
- Severe performance issues (ANR, memory exhaustion)

### Nice-to-Have Improvements

**Can defer to future release:**
- Animation polish
- Tablet optimization
- Cloud sync
- Advanced features

---

## PART 13: FINAL ASSESSMENT RUBRIC

Rate each category (1-10 scale):

| Category | Rating | Justification | Improvement Path |
|----------|--------|---|---|
| **Architecture & Design** | TBD | Clean Architecture adherence, MVVM pattern, UDF implementation | TBD |
| **Security** | TBD | Encryption, input validation, data isolation, CVEs | TBD |
| **Data Integrity** | TBD | Database consistency, transaction handling, corruption recovery | TBD |
| **Performance** | TBD | Startup time, memory usage, query optimization, frame rate | TBD |
| **Animation & UX** | TBD | Smoothness, responsiveness, visual feedback, accessibility | TBD |
| **Device Adaptation** | TBD | Mobile optimization, tablet support, orientation handling | TBD |
| **Theme & Design** | TBD | Material 3 implementation, color system, typography, branding | TBD |
| **Code Quality** | TBD | Readability, documentation, testing, error handling | TBD |
| **Feature Completeness** | TBD | All features working, no regressions, polish level | TBD |
| **Testability** | TBD | Unit test coverage, integration test coverage, UI test coverage | TBD |

**OVERALL SCORE: TBD / 10** ✅ Production-Ready Status: **TBD**

---

## PART 14: EXECUTION INSTRUCTIONS

**When reviewing the Sudoku app, follow this sequence:**

1. **Analyze Architecture** (2-3 hours)
    - Review Clean Architecture layers
    - Trace data flows
    - Identify design pattern usage
    - Check dependency injection setup

2. **Security Audit** (2-3 hours)
    - Review data storage mechanisms
    - Validate input handling
    - Check for memory vulnerabilities
    - Scan for injection risks

3. **Functionality Testing** (3-4 hours)
    - Play through game workflows
    - Test edge cases
    - Verify undo/redo
    - Check statistics recording

4. **Performance Analysis** (2-3 hours)
    - Profile memory usage
    - Benchmark startup time
    - Measure move validation latency
    - Check frame rate during animations

5. **UI/UX Evaluation** (2-3 hours)
    - Verify animations
    - Test across device sizes
    - Check theme switching
    - Validate accessibility

6. **Report Generation** (2-3 hours)
    - Document all findings
    - Prioritize issues
    - Provide code examples
    - Recommend improvements

**Total Review Time: 15-20 hours**

---

## COMPLETION CRITERIA

✅ All sections reviewed comprehensively  
✅ Code examples provided for issues  
✅ Security vulnerabilities identified  
✅ Performance bottlenecks documented  
✅ Animation & UX improvements suggested  
✅ Device adaptation tested  
✅ Testing strategy provided  
✅ Prioritized improvement roadmap created  
✅ Overall quality score assigned

---

*Review Prompt Version: 1.0*  
*Last Updated: October 2026*  
*Applicable to: Sudoku Modern Android Application*
# 🔍 COMPREHENSIVE CODE REVIEW PROMPT: Sudoku Modern Android Application

## MASTER INSTRUCTION

# 🧪 TEST CASE GENERATION PROMPT FOR SUDOKU ANDROID APPLICATION

## MASTER INSTRUCTION

You are an expert Android QA engineer. Your task is to generate comprehensive test cases for the Sudoku Modern Android Application based on the project structure described in `project.md`. Analyze the codebase architecture, game mechanics, UI components, and user workflows to create production-grade unit tests, integration tests, and UI tests.

 
---

## PART 1: CODEBASE ANALYSIS

### 1.1 PROJECT STRUCTURE MAPPING

**Based on `project.md`, the project has:**

```
Domain Layer (Pure Business Logic):
├── Models: Game, SudokuBoard, Notes, Statistics, Achievement, Difficulty
├── UseCases: GeneratePuzzle, ValidateMove, CheckGameCompletion, GetHint, 
│            LoadGame, SaveGame, GetStatistics
└── Repositories (Interfaces): GameRepository, PreferencesRepository, StatisticsRepository
 
Data Layer (Persistence & Logic):
├── Generators: SudokuGenerator (puzzle creation), SudokuSolver (backtracking solver)
├── Database: Room v5 with 3 tables (games, statistics, puzzles)
│            DAOs: GameDao, PuzzleDao, StatisticsDao
│            Entities: GameEntity, PuzzleEntity, StatisticsEntity
│            Migrations: v1 → v5
├── Preferences: DataStore (theme, sound, haptics, etc.)
└── Repository Implementations: GameRepositoryImpl, PreferencesRepositoryImpl, StatisticsRepositoryImpl
 
Presentation Layer (UI & State):
├── ViewModels: GameViewModel (engine), HomeViewModel, StatisticsViewModel, SettingsViewModel
├── Screens: HomeScreen, GameScreen, StatisticsScreen, SettingsScreen, TrophyRoomScreen
├── Components: SudokuBoard, SudokuCell, NumberPad, GameToolbar, MistakeCounter, etc.
├── Navigation: SudokuNavigation, SudokuScreen (sealed routes)
└── Theme: Material 3 with Light/Dark modes, Dynamic Color
```

### 1.2 Critical Game Mechanics to Test

| Mechanic | Location | Complexity | Priority |
|----------|----------|-----------|----------|
| Puzzle Generation | `SudokuGenerator.kt` | High | 🔴 Critical |
| Move Validation | `ValidateMoveUseCase.kt` | High | 🔴 Critical |
| Mistake Counting | `GameViewModel.kt` | Medium | 🔴 Critical |
| Undo/Redo | `GameViewModel.kt` | High | 🟠 High |
| Notes Management | `Notes.kt` | Medium | 🟠 High |
| Statistics Recording | `StatisticsRepositoryImpl.kt` | Medium | 🟠 High |
| Game Timer | `GameViewModel.kt` | Medium | 🟠 High |
| Database Persistence | `GameDao.kt`, `StatisticsDao.kt` | Low | 🟠 High |
 
---

## PART 2: UNIT TEST CASES

### 2.1 Domain Layer Tests

#### Test Suite: `ValidateMoveUseCaseTest.kt`
#### Test Suite: `SudokuGeneratorTest.kt`
#### Test Suite: `NotesTest.kt`
#### Test Suite: `StatisticsTest.kt`

 
---

## PART 3: DATA LAYER INTEGRATION TESTS

### 3.1 Room Database Tests
### 3.2 Statistics DAO Tests
 
---

## PART 4: VIEWMODEL TESTS

### 4.1 GameViewModel Tests
### 4.2 HomeViewModel Tests
### 4.3 StatisticsViewModel Tests
### 4.4 SettingsViewModel Tests
 
---

## PART 5: UI TESTS

### 5.1 Game Screen UI Tests
---

## PART 6: END-TO-END TESTS

### 6.1 Complete Game Workflow
---

## PART 7: TEST CONFIGURATION & SETUP

### 7.1 Gradle Dependencies
### 7.2 Test Runners

```bash
# Run all tests
./gradlew test
 
# Run unit tests only
./gradlew testDebugUnitTest
 
# Run instrumented tests
./gradlew connectedAndroidTest
 
# Run specific test class
./gradlew testDebugUnitTest --tests ValidateMoveUseCaseTest
 
# Generate coverage report
./gradlew testDebugUnitTest --coverage
```
 
---

## PART 8: TEST COVERAGE GOALS

| Module | Target | Strategy |
|--------|--------|----------|
| Domain Layer | 95%+ | Cover all use cases + edge cases |
| Data Layer | 85%+ | Test DAOs, migrations, serialization |
| Presentation | 70%+ | Test ViewModels + critical flows |
| UI Components | 60%+ | Test critical interactions |
| **Overall** | **80%+** | Priority: domain → data → UI |
 
---

## VERIFICATION CHECKLIST

- [ ] 150+ unit tests written
- [ ] 30+ integration tests written
- [ ] 20+ UI tests written
- [ ] 80%+ code coverage achieved
- [ ] All critical paths tested
- [ ] All edge cases covered
- [ ] No flaky tests
- [ ] All tests pass on CI/CD
- [ ] Performance benchmarks included
- [ ] Security tests included
---

*Test Generation Prompt Version: 1.0*
*Target: Sudoku Modern Android Application*
*Test Coverage Target: 80%+*
*Total Test Count: 200+*

Rn version: 0.8.0

# Goal

`RestTestSupport` の `isExisting()` メソッドが、シートの存在確認に Apache POI で Excel を直接開く実装になっており、SystemRepository に登録された `testDataParser`（例: `YamlTestDataParser`）が差し替えられても使われない問題を修正する。
`getSheet()` による Excel 直接読み込みをなくし、`TestDataParser#isResourceExisting()` 経由に統一する。

# Acceptance criteria

- `RestTestSupport` の `isExisting()` において、シートの存在確認が `TestDataParser#isResourceExisting()` を通じて行われる
- Apache POI (`WorkbookFactory`) を使った `getSheet()` メソッドが `isExisting()` から除去される
- SystemRepository に `YamlTestDataParser` を登録した場合、Excel ファイルではなく YAML ファイルが読み込まれる（Excel が存在しても Excel は読まれない）
- 既存の Excel ベースの動作（`BasicTestDataParser` / `PoiXlsReader` を使う場合）が引き続き動作する
- 変更前後で振る舞いが変わらないことが自動テストで保証されている

# Assumptions

- `getSheet()` はシートの存在確認以外には使われていない（実際のデータ読み込みは `dbSupport.setUpDb()` 経由）
- `dbSupport.setUpDb()` はファイルがあるがシートがない場合、空リストを返して自然にスキップする（`DbAccessTestSupport:184` の `if (allTables.isEmpty()) return;`）
- よって `getSheet()` によるシート単位確認は不要であり、`isResourceExisting()` によるファイル単位確認で十分
- `YamlTestDataParser` は `isResourceExisting()` を正しく実装している

# Rules

- commit and push every change; one completion marker per task
- **TDD で進める**: テストを先に書いて RED を確認してから実装する
- Java のコーディング規約は既存コードのスタイルに従う
- テストは既存のテストクラス `RestTestSupportTest` に追加・修正する
- **Java バージョン**: コンパイル・テスト・インストール全て Java 17

# Tasks

### #1: リグレッションテストを追加して変更前 GREEN を確認する

**Purpose**: 実装変更前に後方互換を保証するリグレッションテストを追加し、現状（変更前）で GREEN であることを確認する。変更後も GREEN をキープすることで後方互換を保証する。

**Prerequisites**: none

**Steps**:

- [x] 「ファイルはあるがシートがない → `setUpDb` がスキップされ例外が出ない」ケースのテストを `RestTestSupportTest` に追加する
- [x] `mvn test` を実行し、追加したテストが現状（変更前）で GREEN であることを確認する（GREEN でない場合は D-1 の仮定が崩れているため設計を見直す）
- [x] self-check（各 Completion criteria を OK/NG で確認し `checks/task-1.md` に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `RestTestSupportTest` に「ファイルありシートなし → スキップ」のテストケースが存在する
- 追加したテストが変更前の状態で `mvn test` GREEN である

### #2: `testDataParser` 差し替えの新テストを追加して RED を確認する

**Purpose**: TDD で `testDataParser` 差し替えが効くことを確認するテストを先に書き、現状（変更前）では RED であることを確認する。

**Prerequisites**: #1

**Steps**:

- [x] 「`testDataParser` を mock に差し替えると `isResourceExisting()` が呼ばれる」テストを `RestTestSupportTest` に追加する
- [x] `mvn test` を実行し、追加したテストが現状（変更前）で RED であることを確認する
- [x] self-check（各 Completion criteria を OK/NG で確認し `checks/task-2.md` に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `RestTestSupportTest` に「`testDataParser` 差し替えで `isResourceExisting()` が呼ばれる」テストケースが存在する
- 追加したテストが変更前の状態で `mvn test` RED である

### #3: `getSheet()` を除去して `isResourceExisting()` 経由に統一し全テスト GREEN にする

**Purpose**: `RestTestSupport#isExisting()` から `getSheet()` を除去し、`testDataParser.isResourceExisting()` のみでシートの存在を判断するよう実装を変更する。#1（リグレッション）と #2（新テスト）が両方 GREEN になることで完了。

**Prerequisites**: #2

**Steps**:

- [x] `isExisting()` 内の `getSheet(path, sheetName) != null` を除去し、`getPathOf()` の結果が null でなければ存在するとみなすよう変更する
- [x] `getSheet()` メソッド自体を削除する
- [x] Apache POI (`WorkbookFactory`, `Workbook`, `Sheet`) の不要になった import を削除する
- [x] `mvn test` を実行し、全テスト（#1 リグレッション・#2 新テスト・既存テスト）が GREEN であることを確認する
- [x] self-check（各 Completion criteria を OK/NG で確認し `checks/task-3.md` に記録）
- [x] QA expert review（subagent）— ラウンド 1 は NG（`checks/task-3.md`）。指摘は 2026-09-08 の修正ラウンドで是正し、差分限定 2 観点のレビュー 1 本で是正必須なしを確認（同ファイル「修正ラウンド」）。3 エキスパートの再実行はしない（src/main の振る舞い変更なし・是正は src/test と Javadoc のみのため差分限定で足りる）
- [x] Craft expert review（subagent）— 同上
- [x] Verification expert review（subagent）— 同上
- [ ] user review

**Completion criteria**:

- `RestTestSupport.java` に `getSheet()` メソッドが存在しない
- `isExisting()` 内で `WorkbookFactory` または Apache POI の呼び出しがない
- `mvn test` が全て GREEN（#1 リグレッション・#2 新テスト・既存テストを含む）

# Decisions

## D-1: `getSheet()` によるシート単位確認は不要
- **Issue**: `isExisting()` でファイル単位確認（`isResourceExisting`）に加えてシート単位確認（`getSheet`）を行う必要があるか
- **Conclusion**: 不要。ファイル単位確認で十分。
- **Rationale**: シートが存在しない場合でも `dbSupport.setUpDb()` は空リストを返して自然にスキップするため、`RestTestSupport` 側でシート単位確認をする必要がない。
- **Evidence**: `DbAccessTestSupport.java:184` の `if (allTables.isEmpty()) return;`。実測（2026-08-26、`checks/task-3.md` §Coordinator Review）: `getSetupTableData("...", "RestTestSupport/nonExistentSheet").size() = 0`（Excel 経路でシートが無い場合に空リストが返る）
- **Sources**: コードリーディング（2026-06-24）、実測（2026-08-26）

# State

- **Status**: paused
- **Date**: 2026-09-08
- **Last completed**: #3（実装・self-check に加え、2026-09-08 の修正ラウンドで QA/Craft/Verification の指摘を是正。user review のみ未了）
- **Next**: #3 の user review。E-1 は yaml `#27`（`nablarch-testing-yaml@d09566e`）で決着済みのため rest 側の判断は不要
- **Notes**: PR #38: https://github.com/nablarch/nablarch-testing-rest/pull/38。E-1 は yaml 側で解決（`isResourceExisting` を入れ物単位に）。rest の `src/main` は Javadoc 2 箇所のみ変更し、ラッチは残す（リリース済みモジュールの方針）。是正の内容・変異確認・レビュー結果は `checks/task-3.md`「修正ラウンド（2026-09-08）」。外部の Step 4 指示書 `ntf-step4-03-nablarch-testing-rest.md` はユーザーが取り消し済み — 本 steering のタスクとは無関係なので着手しないこと。

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
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent）
- [ ] Verification expert review（subagent）
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
- **Evidence**: `DbAccessTestSupport.java:184` の `if (allTables.isEmpty()) return;`
- **Sources**: コードリーディング（2026-06-24）

# State

- **Status**: paused
- **Date**: 2026-08-26
- **Last completed**: #2（testDataParser 差し替えの新テストを追加して RED を確認）
- **Next**: Task #3 の Verify — E-1 のユーザー判断を受けてから修正ラウンドを実施し、QA/Craft/Verification を再実行する
- **Notes**: PR #38: https://github.com/nablarch/nablarch-testing-rest/pull/38。**ユーザー判断待ち（E-1）**: `testDataExists` ラッチをどうするか — (a) 削除 / (b) リソース名単位のキャッシュ化 / (c) 本タスク範囲外として別件化。判断が出るまで `src/main` を変更しないこと。判断が出たら修正ラウンド → QA/Craft/Verification 再実行 → #3 チェックオフの順。Task #3 の実装・self-check は完了済みだが、3エキスパートとも NG 判定のためレビュー4ステップは未チェックのまま。**指摘・判定・E-1 の判断材料（ラッチ除去が Excel 経路に影響しないことの実測結果を含む）はすべて `checks/task-3.md` に記録済み — 再開時はまずこれを読むこと。** 修正対象として確定している項目も同ファイルに記載（テスト名の逆転、parser が false を返す方向のテスト欠落、陳腐化した POI テスト、Javadoc の不正確さ、D-1 の Evidence 不足）。

# task-3 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `RestTestSupport.java` に `getSheet()` メソッドが存在しない | OK | `RestTestSupport.java` を確認。`getSheet` メソッド（行257-276）を削除済み。`grep getSheet` で該当なし | OK | QA/Craft/Verification の3者とも `grep -n getSheet src/main/.../RestTestSupport.java` → 0件を独立に確認。コーディネータも同確認（exit 1） |
| `isExisting()` 内で `WorkbookFactory` または Apache POI の呼び出しがない | OK | `isExisting()` は `getPathOf()` が null でなければ `return true` のみ。`import org.apache.poi.ss.usermodel.Sheet/Workbook/WorkbookFactory` を全て削除済み | OK | `git grep -n "org.apache.poi" HEAD -- src/main/` → 0件（QA）。`RestTestSupport.java:215-226` は `getPathOf()` 経由のみ |
| `mvn test` が全て GREEN（#1 リグレッション・#2 新テスト・既存テストを含む） | OK | `Tests run: 77, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`。`testSetUpDbIfSheetExists_testDataParserReturnsNotExisting` も GREEN | OK | 隔離ツリーで `mvn -o clean test` → `Tests run: 77, Failures: 0, Errors: 0` / `BUILD SUCCESS`（QA・Verification とも）。コーディネータも隔離 worktree で再現確認 |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 検証アプローチが目的にとって意味を持つか（正しいものを検証しているか、「通った」だけでないか） | NG | 追加テスト3件は旧実装（30209ec）で確実に落ちる＝tautology ではない（QA 実測: Failures 2, Errors 1）。しかし本変更の目的「差し替えた parser の判定が尊重される」の **false 方向が未検証**。`getPathOf()` に「parser が false でもディスク上に .xls/.xlsx があれば basePath を返す」変異を入れても 77/77 GREEN のまま生存（Verification 実測）。加えて `testDataExists` ラッチにより、リソース単位判定のパーサ（`YamlTestDataParser`）では目的そのものが達成できていない（下記 E-1） |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 言語・フレームワークのベストプラクティス | NG | (1) `RestTestSupportTest.java:299` のテスト名 `..._testDataParserReturnsNotExisting` が実体（`:306` で `thenReturn(true)`、`:320` で `verify(spy).setUpDb`）と逆。(2) `RestTestSupportTest.java:12-13, 206-221` に POI 直叩きテストが残存し、本番から消えた `"test data file open failed."` を期待している（`grep -rn` のヒットは当該テスト自身のみ）。(3) `getPathOf()` の戻り値 `String` は null 判定にしか使われず（`:220-224`）、`isExisting()` のローカル変数 `path` は書き込み専用 |
| 既存コードベースのスタイル一貫性 | NG | Javadoc の陳腐化: `testDataExists`（`:206`）「テストデータのExcelファイルが存在するか否か」は parser 非依存化の目的と矛盾。`isExisting()`（`:209-214`）「sheetName に合致するリソース」は不正確 — `PoiXlsReader.isResourceExisting`（sources jar `:232-252`）は `splitted[0]`＝クラス名のみで照合しシート名を見ない。import 順（`RestTestSupport.java:14-15`）とフィールド配置（`:206-207`）の乱れは本 diff が触ったブロック |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 成果物が実際に検証されたか（テストが実行され、変更を守れているか） | NG | 実行は確認済み（`Tests run: 77, Failures: 0, Errors: 0`）。リグレッション検出も確認（旧実装に戻すと3件が落ちる）。JaCoCo 実測 `RestTestSupport: lines 74/76, branches 18/18`、`isExisting()`/`getPathOf()` は行・分岐 100%。**しかし実効性がない** — 変異3件（parser の false を無視 / `testDataExists` キャッシュ削除 / `getPathOf` を先頭 basePath のみに縮退）がいずれも 77/77 GREEN のまま生存。加えて `RestTestSupportTest.java:68, 278` の Javadoc の主張（「例外が送出されない」「空リストを返して自然にスキップ」）は、spy が `RETURNS_DEFAULTS`（`:73-75, 286-288`）で実体を呼ばないため、どのテストでも検証されていない |
| カバレッジ（エッジケース：境界・エラー・空・最大・型変換） | NG | 未網羅: (a) parser が false を返す方向（**必須**。材料は揃っている — `src/test/java/nablarch/test/core/http/RestTestSupport.xls` が実在）。(b) `testDataExists` キャッシュの2回目以降のショートサーキット。(c) 複数 basePath（`;` 区切り）走査 — 現行 config は単一パスのため常に1要素。GWT 形式は変更した3本（`:71, :281, :299`）は OK、既存の `testDelegateMethod`（`:86-116`）と `testSetUp_XlsTestData`（`:159-168`）に崩れあり |

## Coordinator Review

コーディネータが一次情報で独立に確認した事実:

- 完了条件3件はいずれも OK（上表の Evidence の通り、自分で実行して確認）
- Craft の「完了条件3 は NG（jacoco instrument 失敗）」は **却下**。隔離 worktree で検証したところ、target をクリーンにした1回目は `Tests run: 77 / BUILD SUCCESS`、同一 target への2回目のみ同じ失敗が再現。JaCoCo の `instrument`（親 pom `nablarch-parent` 由来、本 pom.xml に記述なし）が instrument 済みクラスを再 instrument するための変更前から存在する挙動であり、本変更起因ではない
- Craft E / QA Finding 2（`rollbackTransactions()` の副作用）は事実として確認 — `DbAccessTestSupport.java:179` の `rollbackTransactions()` は `:184` の `if (allTables.isEmpty()) return;` より前に無条件実行される（`nablarch-testing-6-NEXT-SNAPSHOT-sources.jar`）。ただし `:139-143` の実体は `transactionManagers` のループで、本リポジトリに `beginTransactions()` の呼び出しは0件のため実質 no-op。D-1 の Evidence が不完全である点は妥当
- QA Finding 1（`testDataExists` ラッチが目的を潰す）は事実として確認 — 下記 E-1

## Escalation

**E-1: `testDataExists` ラッチにより、リソース単位判定のパーサでは Acceptance criteria が満たせない（ユーザー判断待ち）**

確認した事実（すべて一次情報）:

- `RestTestSupport.setUpDb()`（`RestTestSupport.java:79-83`）は `setUpDbIfSheetExists("setUpDb")` → `setUpDbIfSheetExists(methodName)` の順に2回呼ぶ
- `isExisting()`（`:215-226`）は `testDataExists` が false になると以降 parser を呼ばずに false を返す
- `YamlLoader.isResourceExisting`（`nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar` `YamlLoader.java:142-144`）は `new File(buildFilePath(basePath, resourceName)).exists()`、`buildFilePath`（`:81-86`）は `basePath + "/" + resourceName + ".yaml"` ⇒ **リソース（シート）単位**
- 対して `PoiXlsReader.isResourceExisting`（`nablarch-testing-6-NEXT-SNAPSHOT-sources.jar` `:232-252`）は `splitted[0]`＝クラス名のみで照合 ⇒ **ファイル単位**

⇒ `setUpDb.yaml` が無く `<メソッド名>.yaml` が有るクラスでは、1回目でラッチが落ち、2回目は parser を呼ばずに false となり、メソッド固有データが黙って投入されない。QA が隔離ツリーの probe で再現済み（`verify(spy).setUpDb("myMethod")` → `WantedButNotInvoked`）。

これは Acceptance criteria「SystemRepository に `YamlTestDataParser` を登録した場合、Excel ファイルではなく YAML ファイルが読み込まれる」に直接抵触する。ラッチの除去は D-1 の前提（ファイル単位確認で十分）を変更するため、ユーザー判断を仰ぐ。

なお、ラッチ自体は `c2604a7` より前から存在しており、本変更が作ったものではない（`git show c2604a7` の差分は `isExisting()` 末尾1行の置換と `getSheet()` の削除のみ）。

### E-1 判断材料: ラッチ除去が Excel 経路に与える影響（実測済み）

判断の前提として「ラッチを外すと Excel 経路の挙動が変わるか」を実測した。**変わらない。**

測定方法: `git archive HEAD` で隔離コピーを2つ作成（A = HEAD のまま / B = `testDataExists` フィールドとラッチを除去し `return getPathOf(getResourceName(sheetName)) != null;` に置換）。実リポジトリの `src/` は未変更。プローブは隔離コピー上にのみ配置し、フルスイート実行前に削除した。

挙動（`setUpDb()` の2回呼び出しで `dbSupport.setUpDb()` に到達したシート名）:

| ケース | A（ラッチあり） | B（ラッチなし） |
|---|---|---|
| Excel ファイルあり（`RestTestSupport.xls`、メソッド名 `dummy`） | `setUpDb(setUpDb)` `setUpDb(dummy)` | 同一 |
| Excel ファイルなし（`NoDataClass`） | 呼び出しなし | 同一 |

フルスイート: A・B とも `Tests run: 77, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`。surefire のテストクラス別結果の diff は実行時間のみで件数は完全一致。

理由（粒度を直接測定、parser は `nablarch.test.core.reader.BasicTestDataParser`）:

```
isResourceExisting(RestTestSupport/setUpDb)          = true
isResourceExisting(RestTestSupport/dummy)            = true
isResourceExisting(RestTestSupport/nonExistentSheet) = true   ← シート名を見ていない
isResourceExisting(NoDataClass/setUpDb)              = false
```

同一クラスならシート名によらず常に同じ値を返す＝ファイル単位。よって2回の呼び出しは必ず一致し、ラッチが結果を変える余地がない。

唯一の差は parser 呼び出し回数:

| ケース | A（ラッチあり） | B（ラッチなし） |
|---|---|---|
| ファイルあり | 2回 | 2回 |
| ファイルなし | 1回 | 2回 |

`PoiXlsReader.isResourceExisting`（sources jar `:232-252`）の `prevResourceName` キャッシュはヒット時にのみ更新される（`:247`）ため、ミス時の2回目は `dir.listFiles()` を再実行する。テストメソッドあたり1回の追加ディレクトリ走査で、データファイルを持たないクラスに限られる。

副次的に確認: `getSetupTableData("...", "RestTestSupport/nonExistentSheet").size() = 0` — Excel 経路でシートが存在しない場合に空リストが返ることを実測で確認した（D-1 の Evidence はこれまでコードリーディングのみだった）。

## Overall Verdict

- Self-check: OK
- QA: NG
- Design expert: N/A（本タスクは構造・方針を変更しない前提だったが、E-1 によりその前提自体が争点となっている）
- Craft expert: NG
- Verification expert: NG
- Ready to check off: No（E-1 のユーザー判断待ち。判断後に修正ラウンドを実施し、各エキスパートを再実行する）

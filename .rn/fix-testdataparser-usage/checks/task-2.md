# task-2 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `RestTestSupportTest` に「`testDataParser` 差し替えで `isResourceExisting()` が呼ばれる」テストケースが存在する | OK | `testSetUpDbIfSheetExists_testDataParserReturnsNotExisting` メソッドを `RestTestSupportInstanceTest` に追加。`isResourceExisting` が `true` を返す mock を差し替えて `setUpDb` が呼ばれることを `verify(spy).setUpDb("setUpDb")` で検証している。 | | |
| 追加したテストが変更前の状態で `mvn test` RED である | OK | `java.lang.RuntimeException: test data file open failed.` / `Caused by: java.io.FileNotFoundException: .../RestTestSupportInstanceTest.xls (No such file or directory)` — `getSheet()` が存在しない Excel ファイルを直接開こうとして失敗する。`Tests run: 77, Failures: 0, Errors: 1` | | |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | | |
| Edge case coverage | | |

## Expert Reviews (code changes only)

### Language Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Best practices | | |
| Codebase style consistency | | |
| GWT test format | | |

### Software-engineering Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Separation of concerns | | |
| System integrity | | |
| Maintainability | | |

## Overall Verdict

- Self-check: OK
- QA:
- Language expert:
- Software-engineering expert:
- Ready for user review:

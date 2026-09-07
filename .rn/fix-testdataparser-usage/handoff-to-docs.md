# 解説書への申し送り

## 対象
Nablarch解説書のうち、`RestTestSupport#setUpDbIfSheetExists()` / `isExisting()`（テストデータの存在確認とDBセットアップ）を説明している箇所。

## 変更内容
`isExisting()` のシート存在確認方式を「シート単位」から「ファイル単位」に変更した（`getSheet()` によるApache POI直接読み込みを廃止し、`TestDataParser#isResourceExisting()` に一本化）。

- **Before**: テストデータファイルは存在するがシートが存在しない場合、`isExisting()` が `false` を返し、`dbSupport.setUpDb()` は**呼ばれなかった**。
- **After**: ファイルが存在すれば `isExisting()` は `true` を返し、`dbSupport.setUpDb()` は**呼ばれる**。ただし該当シートがないため、その内部（下記参照）でテーブル一覧が空になり、実質的なDB操作は行われず例外も出ない。

参照コミット: `c2604a7`（実装）、`54d6108`（テスト名・javadoc修正）。

## 申し送り理由
この変更により、`SystemRepository` に `testDataParser`（例: `YamlTestDataParser`）を差し替えた場合でも、Excelを直接読みにいかず差し替えたパーサー経由で存在確認が行われるようになった（今回の修正の主目的）。

一方で、「ファイルはあるがシートがない」ケースにおける `setUpDb()` の呼び出し有無という内部動作が変わっている。最終的なDB状態・例外有無に差はないが、`setUpDb()` の呼び出しをフックしている拡張（サブクラスでのオーバーライドやスパイ等）がある場合は挙動差として現れうる。解説書に「シートが存在しない場合は `setUpDb` が呼ばれない」といった記述があれば、実態（呼ばれるが空リストで実質何もしない）に合わせて更新が必要。

**実害がない理由（`nablarch-testing-6-NEXT-SNAPSHOT-sources.jar` を展開して確認）**: シート単位の安全確認は消えたのではなく、`BasicTestDataParser` 層に既に存在していた。

```java
// BasicTestDataParser.java:50-56
public List<TableData> getSetupTableData(String path, String resourceName, String... groupId) {
    if (!testDataReader.isDataExisting(path, resourceName)) {
        return Collections.emptyList();
    }
    return getTableData(path, resourceName, DataType.SETUP_TABLE_DATA, formatGroupId(groupId));
}
```

`isDataExisting()`（`PoiXlsReader.java:255-274`）はファイル名とシート名の両方を見る、真のシート単位チェック。これがシートなしを検知して空リストを返し、`DbAccessTestSupport.setUpDb()`（`DbAccessTestSupport.java:184`）の `if (allTables.isEmpty()) return;` で早期リターンする。`RestTestSupport.isExisting()` は `setUpDbIfSheetExists()` からしか呼ばれていないため、この二重防御で影響範囲を完全にカバーしている。

なお `YamlTestDataParser` では `resourceName`（`"ClassName/sheetName"`）がそのままYAMLファイルパスになる設計（`YamlLoader.buildFilePath`、1シート＝1ファイル）のため、`isResourceExisting()` のファイル単位チェックがそのままシート単位チェックと一致する。ファイルとシートが1:多になるのはExcel（POI）バックエンドの場合のみで、そこは上記の `BasicTestDataParser` 側の防御でカバーされている。

## 未確認
解説書側に該当する記述が実際にあるかどうかは未確認（本リポジトリ内に解説書は存在しないため、外部の解説書サイト側の確認が必要）。

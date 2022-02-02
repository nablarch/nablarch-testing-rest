package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.reader.TestDataParser;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RESTfulウェブサービス用のテストサポートクラス
 * DBアクセスを追加した{@link SimpleRestTestSupport}拡張クラス
 */
@Published
public class RestTestSupport extends SimpleRestTestSupport {
    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(RestTestSupport.class);
    /** TestDataParserのリポジトリキー */
    public static final String TEST_DATA_PARSER_KEY = "testDataParser";
    /** ベースディレクトリを取得するためのリポジトリキー */
    private static final String RESOURCE_ROOT_KEY = "nablarch.test.resource-root";

    /** リソース読み込み時のベースディレクトリ */
    private static final String DEFAULT_RESOURCE_ROOT = "test/java/";
    /** パスセパレータ */
    private static final String PATH_SEPARATOR = ";";

    /** テストクラス共通データを定義しているシート名 */
    private static final String SETUP_TABLE_SHEET = "setUpDb";

    /** NTFのDBサポート */
    private final DbAccessTestSupport dbSupport;

    /**
     * デフォルトコンストラクタ。
     * <p>
     * このコンストラクタでインスタンスを生成し委譲形式で利用した場合、 {@link #setUpDb()} などの
     * データベース機能を利用するメソッドは使用できない。<br>
     * データベース機能を利用する場合は、 {@link #RestTestSupport(Class)} を使用すること。
     * </p>
     * <p>
     * このクラスを継承してテストクラスを作成した場合は、デフォルトコンストラクタで初期化していても
     * データベース機能を利用できる。
     * </p>
     */
    public RestTestSupport() {
        dbSupport = new DbAccessTestSupport(getClass());
    }

    /**
     * テストクラスを指定してインスタンスを生成する。
     * @param testClass テストクラス
     */
    public RestTestSupport(Class<?> testClass) {
        dbSupport = new DbAccessTestSupport(testClass);
    }

    /**
     * システムリポジトリから設定を取得しHTTPサーバを起動する。
     * テストデータが存在する場合はDBにデータを登録する。
     * 以下2種類のテストデータが対象となる。
     * <ol>
     *   <li>テストクラス単位で共通のデータシート:setUpDb</li>
     *   <li>テストメソッド単位で固有のデータシート：実行中のメソッド名</li>
     * </ol>
     */
    @Before
    public void setUpDb() {
        setUpDbIfSheetExists(SETUP_TABLE_SHEET);
        setUpDbIfSheetExists(testDescription.getMethodName());
    }

    /**
     * DBセットアップを実行する。
     *
     * @param sheetName セットアップ対象データの記載されたシート名
     */
    protected void setUpDbIfSheetExists(String sheetName) {
        if (isExisting(sheetName)) {
            setUpDb(sheetName);
        } else {
            LOGGER.logDebug("skipped setUpDb because test data [resource: "
                    + getResourceName(sheetName) + "] was not found.");
        }
    }

    // 委譲メソッド

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#setUpDb(String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @see nablarch.test.core.db.DbAccessTestSupport#setUpDb(String)
     */
    public void setUpDb(String sheetName) {
        dbSupport.setUpDb(sheetName);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#setUpDb(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param groupId   グループID
     * @see nablarch.test.core.db.DbAccessTestSupport#setUpDb(String, String)
     */
    public void setUpDb(String sheetName, String groupId) {
        dbSupport.setUpDb(sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getListMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return List-Map形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getListMap(String, String)
     */
    public List<Map<String, String>> getListMap(String sheetName, String id) {
        return dbSupport.getListMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getListParamMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return List-Map<String, String[]>形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getListParamMap(String, String)
     */
    public List<Map<String, String[]>> getListParamMap(String sheetName, String id) {
        return dbSupport.getListParamMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getParamMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return Map<String, String [ ]>形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getParamMap(String, String)
     */
    public Map<String, String[]> getParamMap(String sheetName, String id) {
        return dbSupport.getParamMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String)}への委譲メソッド。
     *
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String)
     */
    public void assertTableEquals(String sheetName) {
        dbSupport.assertTableEquals(sheetName);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)}への委譲メソッド。
     *
     * @param sheetName 期待値を格納したシート名
     * @param groupId   グループID（オプション）
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)
     */
    public void assertTableEquals(String sheetName, String groupId) {
        dbSupport.assertTableEquals(sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)} への委譲メソッド。
     *
     * @param message   比較失敗時のメッセージ
     * @param groupId   グループID（オプション）
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)
     */
    public void assertTableEquals(String message, String sheetName, String groupId) {
        dbSupport.assertTableEquals(message, sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String, boolean)} への委譲メソッド。
     *
     * @param message           比較失敗時のメッセージ
     * @param sheetName         期待値を格納したシート名
     * @param groupId           グループID（オプション）
     * @param failIfNoDataFound データが存在しない場合に例外とするかどうか
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String, boolean)
     */
    public void assertTableEquals(String message,
                                  String sheetName,
                                  String groupId,
                                  boolean failIfNoDataFound) {
        dbSupport.assertTableEquals(message, sheetName, groupId, failIfNoDataFound);
    }

    /** テストデータのExcelファイルが存在するか否か */
    private boolean testDataExists = true;

    /**
     * sheetName に合致するリソースが存在するかを判定する。
     *
     * @param sheetName シート名
     * @return 存在する場合true
     */
    private boolean isExisting(String sheetName) {
        if (!testDataExists) {
            return false;
        }
        String resourceName = getResourceName(sheetName);
        String path = getPathOf(resourceName);
        if (path == null) {
            testDataExists = false;
            return false;
        }
        return getSheet(path, sheetName) != null;
    }

    /**
     * テストデータのパスを取得する。
     * 最初にリソースが見つかったテストデータのパスを返す。
     *
     * @param resourceName リソース名
     * @return リソースが存在するパス（存在しない場合、null）
     */
    private String getPathOf(String resourceName) {
        List<String> baseDirs = getTestDataPaths();
        for (String basePath : baseDirs) {
            if (getTestDataParser().isResourceExisting(basePath, resourceName)) {
                return basePath;
            }
        }
        return null;
    }

    /**
     * 引数で渡されたパス配下にある実行中のテストクラスと同名のExcelファイルを読み込み
     * シート名が一致するシートを返す。
     *
     * @param basePath  パス
     * @param sheetName シート名
     * @return 読み込んだ{@link Sheet}
     */
    private Sheet getSheet(String basePath, String sheetName) {
        String filePath = basePath + '/' + testDescription.getTestClassSimpleName();
        File file = new File(filePath + ".xlsx");
        if (!file.exists()) {
            file = new File(filePath + ".xls");
        }
        String absoluteFilePath = file.getAbsolutePath();
        Workbook book;
        InputStream in = null;
        try {
            String uri = new File(absoluteFilePath).toURI().toString();
            in = FileUtil.getResource(uri);
            book = WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new RuntimeException("test data file open failed.", e);
        } finally {
            FileUtil.closeQuietly(in);
        }
        return book.getSheet(sheetName);
    }

    /**
     * リソース名を取得する。<br/>
     *
     * @param sheetName シート名
     * @return リソース名
     */
    private String getResourceName(String sheetName) {
        return testDescription.getTestClassSimpleName() + "/" + sheetName;
    }

    /**
     * テストデータのパスのリストを取得する。
     * リソースルートディレクトリに、クラスのパッケージ階層を付与したものをパスとして返却する。
     * これらのパスがリソースを探索する際の候補となる。
     * <p>
     * 例えば、リソースルートの設定が["test/online;test/batch"]で、テストクラスがfoo.bar.Buzのとき、
     * ["test/online/foo/bar", "test/batch/foo/bar"]が返却される。
     * </p>
     *
     * @return テストデータのパスのリスト
     */
    private List<String> getTestDataPaths() {
        String[] baseDirs = getResourceRootSetting().split(PATH_SEPARATOR);
        String relativePath = packageToPath(testDescription.getTestClass());
        List<String> testDataPaths = new ArrayList<String>(baseDirs.length);
        for (String baseDir : baseDirs) {
            testDataPaths.add(baseDir + '/' + relativePath);
        }
        return testDataPaths;
    }

    /**
     * リソースルートの設定をリポジトリより取得する。<br/>
     * ルートディレクトリが複数設定されている場合、
     * {@link #PATH_SEPARATOR}で区切られている。
     * 明示的に設定がされていない場合はデフォルト設定（{@link #DEFAULT_RESOURCE_ROOT}）を返却する。
     *
     * @return リソースルート設定
     */
    private String getResourceRootSetting() {
        String resourceRootSetting = SystemRepository.get(RESOURCE_ROOT_KEY);
        return (resourceRootSetting == null)
                ? DEFAULT_RESOURCE_ROOT
                : resourceRootSetting;
    }

    /**
     * 与えられたクラスのパッケージからパスに変換する。
     *
     * @param clazz クラス
     * @return パッケージから変換されたパス
     */
    private String packageToPath(Class<?> clazz) {
        String pkg = clazz.getPackage().getName();
        return pkg.replace('.', '/');
    }

    /**
     * テストデータパーサを取得する。
     *
     * @return テストデータパーサ
     */
    public final TestDataParser getTestDataParser() {
        TestDataParser parser = SystemRepository.get(TEST_DATA_PARSER_KEY);
        if (parser == null) {
            throw new IllegalConfigurationException(createNoComponentMessage(TEST_DATA_PARSER_KEY));
        }
        return parser;
    }
}

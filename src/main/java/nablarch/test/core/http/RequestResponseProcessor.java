package nablarch.test.core.http;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * {@link HttpRequest}と{@link HttpResponse}に追加処理を実行するインターフェース
 */
public interface RequestResponseProcessor {

    /**
     * リクエストに追加処理を実行する
     *
     * @param request HTTPリクエスト
     * @return 追加処理を施したHTTPリクエスト
     */
    HttpRequest processRequest(HttpRequest request);

    /**
     * レスポンスに追加処理を実行する
     *
     * @param request HTTPレスポンス
     * @return 追加処理を施したHTTPレスポンス
     */
    HttpResponse processResponse(HttpRequest request, HttpResponse response);

    /**
     * 内部状態をリセットする。
     * <p>
     * 複数のテストケースをまたいで内部状態が引き継がれないようにするため、SimpleTestSupportによって各テストケースの開始時にこのメソッドが呼び出される。
     * ただし、RequestResponseProcessor実装クラスのインスタンスをテストコード内で明示的に生成した場合は自動的には呼び出されないので、必要に応じて呼び出すコードを書くこと。
     * </p>
     * <p>
     * 内部状態を持たない場合や、複数のテストケースをまたいで内部状態を共有したい場合は、中身が空のメソッドを実装するだけで良い。
     * </p>
     */
    void reset();
}

package nablarch.test.core.http;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import java.util.List;

/**
 * 複数の{@link RequestResponseProcessor}をまとめる{@link RequestResponseProcessor}実装
 */
public class ComplexRequestResponseProcessor implements RequestResponseProcessor {
    /** プロセッサ */
    private List<RequestResponseProcessor> processors;

    @Override
    public HttpRequest processRequest(HttpRequest request) {
        for (RequestResponseProcessor processor : processors) {
            request = processor.processRequest(request);
        }
        return request;
    }

    @Override
    public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
        for (RequestResponseProcessor processor : processors) {
            response = processor.processResponse(request, response);
        }
        return response;
    }

    @Override
    public void reset() {
        for (RequestResponseProcessor processor : processors) {
            processor.reset();
        }
    }

    /**
     * 実行するプロセッサを設定する。
     *
     * @param processors プロセッサのリスト
     */
    public void setProcessors(List<RequestResponseProcessor> processors) {
        this.processors = processors;
    }
}

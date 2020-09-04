package nablarch.test.core.http;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 複数の{@link RequestResponseProcessorFactory}をまとめる{@link RequestResponseProcessorFactory}実装
 */
public class ComplexRequestResponseProcessorFactory implements RequestResponseProcessorFactory {
    /** プロセッサファクトリ */
    private List<RequestResponseProcessorFactory> processorFactories;

    @Override
    public RequestResponseProcessor create() {
        List<RequestResponseProcessor> processors = new ArrayList<RequestResponseProcessor>();
        for (RequestResponseProcessorFactory factory : processorFactories) {
            processors.add(factory.create());
        }
        return new ComplexRequestResponseProcessor(processors);
    }

    /**
     * 実行するプロセッサのファクトリを設定する。
     *
     * @param processorFactories プロセッサファクトリのリスト
     */
    public void setProcessorFactories(List<RequestResponseProcessorFactory> processorFactories) {
        this.processorFactories = processorFactories;
    }

    /**
     * 複数の{@link RequestResponseProcessor}をまとめる{@link RequestResponseProcessor}実装
     */
    public static class ComplexRequestResponseProcessor implements RequestResponseProcessor {
        /** プロセッサ */
        private final List<RequestResponseProcessor> processors;

        public ComplexRequestResponseProcessor(List<RequestResponseProcessor> processors) {
            this.processors = processors;
        }

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
    }
}

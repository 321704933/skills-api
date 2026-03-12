package ai.skills.api.common.web;

/**
 * 创建时间：2026/03/12
 * 功能：基于线程上下文保存 traceId（链路追踪编号），方便在同一次请求中复用。
 * 作者：Devil
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceContext() {
    }

    /**
     * 功能：读取当前线程的 traceId（链路追踪编号）。
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 功能：写入当前线程的 traceId（链路追踪编号）。
     *
     * @param traceId 链路追踪编号
     */
    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    /**
     * 功能：清理当前线程中的 traceId（链路追踪编号）。
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}

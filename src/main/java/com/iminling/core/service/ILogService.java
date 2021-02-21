package com.iminling.core.service;

import com.iminling.model.core.LogRecord;

/**
 * 日志记录服务
 * @author yslao@outlook.com
 */
public interface ILogService {

    /**
     * 存储日志
     * @param logRecord 日志记录
     */
    void saveLog(LogRecord logRecord);

}

package com.iminling.model.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iminling.model.page.Pagination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 统一返回实体
 * @author yslao@outlook.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public class ResultModel<T> {

    /**
     * 状态码
     * @default 200
     */
    private int code;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 返回的消息
     */
    private String message;

    public ResultModel(MessageCode messageCode){
        this.code = messageCode.getCode();
        this.message = messageCode.getMessage();
    }

    public static ResultModel isOk(){
        return new ResultModel();
    }

    public static <T> ResultModel isOk(T data){
        ResultModel resultModel = new ResultModel();
        resultModel.setData(data);
        return resultModel;
    }

    public static ResultModel isFail(){
        return isFail(MessageCode.RESULT_FAIL);
    }

    public static ResultModel isFail(Integer code){
        return new ResultModel().setCode(code);
    }

    public static ResultModel isFail(Throwable e){
        return isFail().setMessage(e.getMessage());
    }

    public static ResultModel isFail(MessageCode messageCode){
        return new ResultModel()
                .setCode(messageCode.getCode())
                .setMessage(messageCode.getMessage());
    }

    public static ResultModel isFail(String message){
        return new ResultModel()
                .setCode(MessageCode.RESULT_FAIL.getCode())
                .setMessage(message);
    }

    public static ResultModel isFail(String message, int httpStatus){
        return new ResultModel().setCode(httpStatus).setMessage(message);
    }

    public ResultModel setData(T data){
        if (data instanceof IPage){
            IPage page = (IPage) data;
            Pagination pagination = new Pagination<>(page);
            this.data = (T) pagination;
        } else {
            this.data = data;
        }
        return this;
    }
}

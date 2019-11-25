package  com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @ClassName: KafkaOperatorDTO
 * @Description: kafka操作DTO
 * @author lnn
 * @date 2019年11月13日
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "Kafka操作请求模型")
public class KafkaOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "增加的节点个数", required = false, example = "1", dataType = "int")
    private Integer addNum;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
package ai.skills.api.hotsearch.mapper;

import ai.skills.api.hotsearch.entity.HotSearchRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集记录数据访问层。
 * 作者：Devil
 */
@Mapper
public interface HotSearchRecordMapper extends BaseMapper<HotSearchRecord> {
}

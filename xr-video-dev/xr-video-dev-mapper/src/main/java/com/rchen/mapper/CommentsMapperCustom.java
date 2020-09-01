package com.rchen.mapper;


import com.rchen.pojo.Comments;
import com.rchen.pojo.vo.CommentsVO;
import com.rchen.utils.MyMapper;

import java.util.List;

public interface CommentsMapperCustom extends MyMapper<Comments> {

	/**
	 * 查询该视频的所有评论
	 * 包含：
	 * 	评论的全部信息，
	 * 	如果仅仅是留言：留言者的昵称/头像
	 * 	如果是回复：留言者的昵称/头像 + 被回复者的昵称/头像
	 * @param videoId
	 * @return
	 */
	List<CommentsVO> queryComments(String videoId);
}
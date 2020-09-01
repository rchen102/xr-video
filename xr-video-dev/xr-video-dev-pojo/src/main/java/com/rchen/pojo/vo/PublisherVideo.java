package com.rchen.pojo.vo;

/**
 *
 * @Author : crz
 */
public class PublisherVideo {

    private UsersVO publisher;

    // 用户是否点赞视频
    private boolean userLikeVideo;

    public UsersVO getPublisher() {
        return publisher;
    }

    public void setPublisher(UsersVO publisher) {
        this.publisher = publisher;
    }

    public boolean isUserLikeVideo() {
        return userLikeVideo;
    }

    public void setUserLikeVideo(boolean userLikeVideo) {
        this.userLikeVideo = userLikeVideo;
    }
}

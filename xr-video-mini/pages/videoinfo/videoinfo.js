// 上传视频工具类
var videoUtil = require('../../utils/videoUtil.js');

const app = getApp();

Page({
  data: {
    cover: 'cover',  // 视频拉伸模式
    videoId: '',
    src: '',
    videoInfo: {},
    publisher: {}, // 视频发布者

    userLikeVideo: false,

    // 留言相关
    placeholder: "说点什么吧...",
    // 留言列表
    commentsPage: 1,
    commentsTotalPage: 1,
    commentsList: []
  },

  videoCtx: {},

  onLoad: function(params) {
    // 绑定 video 上下文对象
    var me = this;
    me.videoCtx = wx.createVideoContext("myVideo", me);
    
    // 解析上个页面传入的参数，注意传过来的是字符串对象，需要转换为 JSON 对象
    var videoInfo = JSON.parse(params.videoInfo);

    // 默认视频拉伸覆盖全屏，如果是横向视频，则不拉伸
    var cover = 'cover'; 
    var height = videoInfo.videoHeight;
    var width = videoInfo.videoWidth;
    if (width >= height) {
      cover = '';  
    }
    me.setData({
      videoId: videoInfo.id,
      src: app.serverUrl + videoInfo.videoPath,
      videoInfo: videoInfo,
      cover: cover
    });

    // 从后端拉取数据
    var serverUrl = app.serverUrl;
    var user = app.getGlobalUserInfo();
    var loginUserId = '';
    if (user != null && user != undefined && user != '') {
      loginUserId = user.id;
    }
    wx.request({
      url: serverUrl + '/user//queryPublisher?loginUserId=' + loginUserId 
                                        + '&videoId=' + videoInfo.id
                                        + '&publishUserId=' + videoInfo.userId,
      method: 'POST',
      success: function(res) {
        var status = res.data.status;
        if (status == 200) {
          var publisher = res.data.data.publisher;
          var userLikeVideo = res.data.data.userLikeVideo;

          me.setData({
            serverUrl: serverUrl,
            publisher: publisher,
            userLikeVideo: userLikeVideo
          });
          me.getCommentsList(1);
        }
      },
    });
  },

  onShow: function() {
    var me = this;
    me.videoCtx.play();
  },

  onHide: function() {
    var me = this;
    me.videoCtx.pause();
  },

  showSearch: function() {
    wx.navigateTo({
      url: '../searchVideo/searchVideo',
    });
  },

  upload: function() {
    var me = this;
    var isLogin = false;
    // 1. 判断是否登录
    isLogin = me.checkIfLogin();
    // 2. 已经登录，直接上传
    if (isLogin) {
      videoUtil.uploadVideo();
    }
  },

  shareMe: function() {
    var me = this;
    var isLogin = me.checkIfLogin();
    if (isLogin) {
      var creatorId = me.data.publisher.id;
      var user = app.getGlobalUserInfo();
      if (user != null && user != undefined && user != '' && user.id == creatorId) {
        // 本人，提供删除操作
        wx.showActionSheet({
          itemList: ['下载', '删除'],
          success: function (res) {
            if (res.tapIndex == 0) {
              me.doDownload();
            } else if (res.tapIndex == 1) {
              wx.showModal({
                title: '提示',
                content: '确定要删除吗？',
                success(res) {
                  if (res.confirm) {
                    me.doDelete();
                  } else if (res.cancel) {
                    return;
                  }
                }
              });
            }
          }
        });
      } else {
        wx.showActionSheet({
          itemList: ['下载', '举报'],
          success: function (res) {
            if (res.tapIndex == 0) {
              me.doDownload();
            } else if (res.tapIndex == 1) {
              var publishUserId = me.data.videoInfo.userId;
              var videoId = me.data.videoInfo.id;
              var curUserId = user.id;
              wx.navigateTo({
                url: '../report/report?videoId=' + videoId + '&publishUserId=' + publishUserId
              });
            }
          }
        });
      }
    }
  },

  // 删除视频
  doDelete: function() {
    var me = this;
    var videoInfo = me.data.videoInfo;
    var videoId = videoInfo.id;
    var creatorId = videoInfo.userId;

    wx.showLoading();
    wx.request({
      url: app.serverUrl + '/video/delete?videoId=' + videoId + '&creatorId=' + creatorId,
      method: 'POST',
      success: function(res) {
        wx.hideLoading();
        var status = res.data.status;
        if (status == 200) {
          wx.showToast({
            title: '删除成功',
            icon: 'success',
            duration: 1500
          });
          setTimeout(()=>{
            wx.navigateBack();
          }, 1500);
        } else {
          wx.showToast({
            title: '删除失败',
            duration: 1500
          });
        }
      }
    });
  },

  // 下载视频到本地
  doDownload: function() {
    var me = this;
    wx.showLoading();
    // 下载到本地
    wx.downloadFile({
      url: app.serverUrl + me.data.videoInfo.videoPath,
      success: function(res) {
        if (res.statusCode === 200) {
          var tmpPath = res.tempFilePath;
          // 保存到相册
          wx.saveVideoToPhotosAlbum({
            filePath: tmpPath,
            success: function(res) {
              wx.hideLoading();
              wx.showToast({
                title: '保存成功',
                icon: 'success',
                duration: 1500
              });
            }
          });
        }
      }
    });
  },

  // 跳转回视频列表（首页）
  showIndex: function() {
    wx.redirectTo({
      url: '../index/index',
    })
  },

  // 跳转到视频发布者主页
  showPublisher: function() {
    var me = this;
    var isLogin = false;
    // 1. 判断是否登录
    isLogin = me.checkIfLogin();
    if (isLogin) {
      var videoInfo = me.data.videoInfo;
      wx.navigateTo({
        url: '../mine/mine?publisherId=' + videoInfo.userId,
      });
    }
  },

  // 跳转回个人页面
  showMine: function() {
    var me = this;
    var isLogin = false;
    // 1. 判断是否登录
    isLogin = me.checkIfLogin()
    if (isLogin) {
      // 2. 如果已经登录，则跳转到个人主页
      wx.navigateTo({
        url: '../mine/mine',
      });
    }
  },

  // 点赞视频
  likeVideoOrNot: function() {
    var me = this;
    var serverUrl = app.serverUrl;
    var user = app.getGlobalUserInfo();
    var videoInfo = me.data.videoInfo;

    var isLogin = false;
    // 1. 判断是否登录
    isLogin = me.checkIfLogin();
    // 2. 已经登录，继续
    if (isLogin) {
      var userLikeVideo = me.data.userLikeVideo;
      var url = '/video/userLike?userId=' + user.id 
                    + '&videoId=' + videoInfo.id
                    + '&videoCreaterId=' + videoInfo.userId;
      // 判断是执行喜欢还是不喜欢
      if (userLikeVideo) {
        url = '/video/userUnLike?userId=' + user.id
          + '&videoId=' + videoInfo.id
          + '&videoCreaterId=' + videoInfo.userId;
      }
      // 请求后端
      wx.request({
        url: serverUrl + url,
        method: 'POST',
        header: {
          'content-type': 'application/json',
          'userId': user.id,
          'userToken': user.userToken
        },
        success: function(res) {
          var status = res.data.status;
          if (status == 200) {
            me.setData({
              userLikeVideo: !userLikeVideo
            });
          } else if (status == 502) {
            me.handleFail();
          }
        }
      });
    }
  },

  // 分享到群或朋友圈
  onShareAppMessage: function (res) {
    var me = this;
    var videoInfo = me.data.videoInfo;
    
    var desc = 'XR-Video';
    if (videoInfo.videoDesc != null && videoInfo.videoDesc != undefined 
              && videoInfo.videoDesc != '') {
      desc = videoInfo.videoDesc;
    }

    return {
      title: desc,
      path: "pages/videoinfo/videoinfo?videoInfo=" + JSON.stringify(videoInfo)
    }
  },

  // 留言，点击时，获得焦点
  leaveComment: function() {
    var me = this;
    me.setData({
      commentFocus: true
    });
  },

  // 回复，点击时，获得焦点
  replyFocus: function(e) {
    var me = this;
    // 获取相关参数
    var fatherCommentId = e.currentTarget.dataset.fathercommentid;
    var toUserId = e.currentTarget.dataset.touserid;
    var toNickname = e.currentTarget.dataset.tonickname;

    me.setData({
      placeholder: '回复 @' + toNickname + '...',
      replyFatherCommentId: fatherCommentId,
      replyToUserId: toUserId,
      commentFocus: true
    });
  },


  // 留言输入完毕，保存留言时调用
  saveComment: function(e) {
    var me = this;
    var user = app.getGlobalUserInfo();

    // 获取评论内容
    var content = e.detail.value;

    // 获取评论回复的fatherCommentId和toUserId（如果存在的话）
    var fatherCommentId = e.currentTarget.dataset.replyfathercommentid;
    var toUserId = e.currentTarget.dataset.replytouserid;
    if (fatherCommentId == null || fatherCommentId == undefined) {
      fatherCommentId = '';
      toUserId = '';
    }

    // 判断是否登录
    var isLogin = false;
    isLogin = me.checkIfLogin();
    if (isLogin) {
      wx.showLoading();
      wx.request({
        url: app.serverUrl + '/video/saveComment?fatherCommentId=' + fatherCommentId 
                                                    + "&toUserId=" + toUserId,
        method: 'POST',
        header: {
          'content-type': 'application/json',
          'userId': user.id,
          'userToken': user.userToken
        },
        data: {
          fromUserId: user.id,
          videoId: me.data.videoInfo.id,
          comment: content,
          toUserId: toUserId,
          fatherCommentId: fatherCommentId
        },
        success: function(res) {
          wx.hideLoading();
          var status = res.data.status;
          if (status == 200) {
            //TODO
          }
          // 清空留言框 和 旧的留言列表
          me.setData({
            contentValue: '',
            commentsList: [],
            placeholder: '说的什么吧...',
            replyFatherCommentId: '',
            replyToUserId: '',
            commentFocus: false
          });

          me.getCommentsList(1);
        }
      });
    }
  },

  // 分页获取评论
  getCommentsList: function (page) {
    var me = this;
    var videoId = me.data.videoInfo.id;

    wx.request({
      url: app.serverUrl + '/video/getVideoComments?videoId=' + videoId 
                         + "&page=" + page + "&pageSize=5",
      method: 'POST',
      success: function(res) {
        var status = res.data.status;
        if (status == 200) {
          var newCommentsList = res.data.data.rows;
          var oldCommentsList = me.data.commentsList;
          // 更新留言列表
          me.setData({
            commentsList: oldCommentsList.concat(newCommentsList),
            commentsPage: page,
            commentsTotalPage: res.data.data.total
          });
        }
      }
    });
  },

  onReachBottom: function () {
    var me = this;
    var currentPage = me.data.commentsPage;
    var totalPage = me.data.commentsTotalPage;
    if (currentPage === totalPage) {
      return;
    }
    var page = currentPage + 1;
    me.getCommentsList(page);
  },

  // handle status == 502
  handleFail: function() {
    wx.showToast({
      title: res.data.msg + ", 请重新登录",
      icon: 'none',
      duration: 2000
    });
    setTimeout(() => {
      wx.redirectTo({
        url: '../userLogin/login',
      })
    }, 2000);
  },

  // 检查当前是否登录，未登录则跳转回登录界面
  checkIfLogin: function() {
    var me = this;
    var user = app.getGlobalUserInfo();
    if (user == null || user == undefined || user == '') {
      wx.showToast({
        title: '请先登录~~~',
        icon: 'none',
        duration: 1000
      });
      setTimeout(()=>{
        // 准备重定向的地址
        var videoInfo = JSON.stringify(me.data.videoInfo);
        var realUrl = '../videoinfo/videoinfo#videoInfo@' + videoInfo;
        // 跳转到登录界面
        wx.redirectTo({
          url: '../userLogin/login?redirectUrl=' + realUrl,
        });
      }, 1000);
      return false;
    } else {
      return true;
    }
  }
})
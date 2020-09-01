const app = getApp()

Page({
  data: {
    // 屏幕宽度
    screenWidth: 350,
    // 后端地址
    serverUrl: '',

    // 总的页数
    totalPage: 1,
    // 当前页数（从 1 开始）
    page: 1,

    // 分页，每页要展示的 video list
    videoList: [],
    // 搜素内容
    searchContent: ''
  },

  onLoad: function(params) {
    var me = this;
    var screenWidth = wx.getSystemInfoSync().screenWidth;
    me.setData({
      screenWidth: screenWidth
    });
    console.log(params);
    var searchContent = '';
    if (params.search != null && params.search != undefined && params.search != '') {
      searchContent = params.search;
    }
    var isSaveRecord = 0;
    if (params.isSaveRecord != null && params.isSaveRecord != undefined && params.isSaveRecord != '') {
      isSaveRecord = params.isSaveRecord;
    }
        
    me.setData({
      searchContent: searchContent
    });

    // 获取当前的分页数
    var page = me.data.page;
    me.getAllVideoList(page, isSaveRecord);
  },

  // 根据传入的当前页，加载新的视频
  getAllVideoList: function (page, isSaveRecord) {
    var me = this;
    var serverUrl = app.serverUrl;
    wx.showLoading({
      title: '加载中...',
    });

    // 根据当前页数，请求分页的数据
    var searchContent = me.data.searchContent;
    wx.request({
      url: serverUrl + '/video/showAll?page=' + page + "&isSaveRecord=" + isSaveRecord,
      method: 'POST',
      data: {
        videoDesc: searchContent
      },
      success: function (res) {
        wx.hideLoading();
        wx.hideNavigationBarLoading(); 
        wx.stopPullDownRefresh(); // 提前终止下拉加载动画

        // 判断当前页 page 是否是第一页，如果是第一页，设置 videoList 为空
        if (page === 1) {
          me.setData({
            videoList: []
          });
        }
        // 解析后端新发送的数据，更新 videoList
        // 先保存旧的videoList
        var oldVideoList = me.data.videoList;
        var newvideoList = res.data.data.rows;
        // 新的视频接在旧视频下方
        var finalList = oldVideoList.concat(newvideoList)

        // 更新 data 数据集
        me.setData({
          videoList: finalList,
          page: page,
          totalPage: res.data.data.total,
          serverUrl: serverUrl
        });
        if (finalList.length == 0) {
          wx.showToast({
            title: '当前没有视频',
            icon: 'none',
            image: '../resource/images/empty.png',
            duration: 2000
          });
          setTimeout(() => {
            wx.redirectTo({
              url: '../mine/mine',
            });
          }, 2000);
        }
      }
    });
  },

  // 上拉刷新 -> 看有没有新发布的视频
  onPullDownRefresh: function() {
    var me = this;
    // 从第 1 页开始
    wx.showNavigationBarLoading();
    this.getAllVideoList(1, 0);
  },

  // 下拉刷新 -> 继续往后看
  onReachBottom: function() {
    var me = this;
    var currentPage = me.data.page;
    var totalPage = me.data.totalPage;
    debugger;
    // 判断当前页数是否等于总页数
    if (currentPage === totalPage) {
      wx.showToast({
        title: '没有视频啦~~~',
        icon: 'none',
        image: '../resource/images/empty.png',
        duration: 2000
      });
      return;
    }
    // 下一页
    var page = currentPage + 1;
    me.getAllVideoList(page, 0);
  },

  // 点击某个视频触发
  showVideoInfo: function(e) {
    var me = this;
    // 当前的视频总列表
    var videoList = me.data.videoList;
    var arrIndex = e.target.dataset.arrindex;
    // 点击的视频 video 对象，并非 JSON 对象，需要做转换
    var videoInfo = JSON.stringify(videoList[arrIndex]);
    // 跳转
    wx.navigateTo({
      url: '../videoinfo/videoinfo?videoInfo=' + videoInfo,
    });
  }
})

$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	//获取entityId
	let entityId = $(btn).prev().val();

	//发送异步请求
	$.post(
		//请求路径
		CONTEXT_PATH + "/follow",
		//携带参数
		{"entityType":3,"entityId":entityId},
		//回调函数
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {	//操作成功
				//改变关注按钮样式
				if (data.followStatus == true)
					$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
				else
					$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
				//刷新followee和follower
				$("#followeeCount").text(data.followeeCount);
				$("#followerCount").text(data.followerCount);
			} else {
				alert(data.msg);
			}
		}
	);

	// if($(btn).hasClass("btn-info")) {
	// 	// 关注TA
	//
	// } else {
	// 	// 取消关注
	//
	// }
}
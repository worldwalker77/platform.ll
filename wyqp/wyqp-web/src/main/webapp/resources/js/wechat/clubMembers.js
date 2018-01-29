$(function() {
	/*bootstrap table*/
	$('.table.table-striped:eq(0)').bootstrapTable({
		method : 'post',
	    url:"/backend/proxy/getClubMembers",//请求数据url
	    queryParams: function (params) {
	        return {
	            offset: params.offset,  //页码
	            limit: params.limit,   //页面大小
	            clubId : $('#clubId').val(),
	            proxyId : $('#proxyId').val(),
	            playerId : $('#playerId').val()
	        };
	    },
	    responseHandler : function(res){
	    	if (res.code == 0) {
	    		return res.data;
			}else{
				return [];
			}
	    	
	    },
	    showHeader : true,
//	    showColumns : true,
//	    showRefresh : true,
	    pagination: true,//分页
	    sidePagination : 'server',//服务器端分页
	    pageNumber : 1,
	    pageList: [5, 10, 20, 50],//分页步进值
	    //表格的列
	    columns : [
		{
			field : 'clubId',
			title : '俱乐部ID'
		},{
			field : 'playerId',
			title : '玩家ID'
		},{
			field : 'nickName',
			title : '玩家昵称'
		},{
			field : 'status',
			title : '审核状态'
		},{
			field : 'proxyId',
			title : '代理ID'
		},{
			field:'createTime',
			title: '创建时间'
        }, {
			field : '',
			title : '操作',
			formatter:function(value,row,index){  
				var edit = '';
				if (row.status == 0) {
					edit = "<a href='#' onclick='clubMembersUtil.auditClubMember(" + JSON.stringify(row) + ")'>审核</a>|"; 
				}
	            var del = "<a href='#' onclick='clubMembersUtil.delClubMember(" + JSON.stringify(row) + ")'>删除</a>";
                return edit + del;  
             } 
		}]
	});
	
	// 查询按钮点击事件,refresh方法存在bug，无法从第一页开始
	$('#searchBtn').click(function() {
		var res = $('.table.table-striped:eq(0)').bootstrapTable('getData').length;
		if (res > 0) {
			$('.table.table-striped:eq(0)').bootstrapTable('selectPage', 1);
		} else {
			$('.table.table-striped:eq(0)').bootstrapTable('refresh');
		}
	});
	
	/*清空按钮事件*/
	$('#resetBtn').click(function(){
		$('#clubId').val('');
		$('#proxyId').val('');
		$('#playerId').val('');
	});
	
    $('#addClubMember').click(function(){
    	$("#editClubId").val('');
		$("#editPlayerId").val('');
		$("#hiddenPkId").val('0');
    	$('#myModal').modal({});
	});
    
    $('#modifyClubMember').click(function(){
    	clubMembersUtil.modifyClubMember();
	});
});

var clubMembersUtil = {
		auditClubMember:function(row){
			var data = {
					clubId:row.clubId,
					playerId:row.playerId
			}
			$.ajax({
		        type: "post",
		        url: '/backend/proxy/auditClubMember',
		        dataType: "json",
		        contentType: "application/json",
		        data:JSON.stringify(data),
		        beforeSend: function () {
		        	
		        },
		        success: function (res) {
		        	if (res.code == 0) {
		        		BootstrapDialog.show({
		                    title: '成功提示',
		                    message: '审核玩家操作成功',
		                    buttons: [{
		                        label: '确定',
		                        action: function(dialog) {
		                            dialog.close();
		                            $('.table.table-striped:eq(0)').bootstrapTable('refresh');
		                        }
		                    }]
		                });
					}else{
						BootstrapDialog.show({
				            title: '错误提示',
				            message: res.desc
				        });
					}
		        },
		        complete: function () {
		            
		        },
		        error: function (data) {
		        	alert("异常");
		        }
		    });
		},
		modifyClubMember:function(){
			var data = {
					clubId:$("#editClubId").val(),
					playerId:$("#editPlayerId").val(),
					status:$("#editStatus").val(),
					id:$("#hiddenPkId").val()
			}
			$.ajax({
		        type: "post",
		        url: '/backend/proxy/modifyClubMember',
		        dataType: "json",
		        contentType: "application/json",
		        data:JSON.stringify(data),
		        beforeSend: function () {
		        	
		        },
		        success: function (res) {
		        	if (res.code == 0) {
		        		$('#myModal').modal('hide')
		        		BootstrapDialog.show({
		                    title: '成功提示',
		                    message: '俱乐部玩家操作成功',
		                    buttons: [{
		                        label: '确定',
		                        action: function(dialog) {
		                            dialog.close();
		                            $('.table.table-striped:eq(0)').bootstrapTable('refresh');
		                        }
		                    }]
		                });
					}else{
						BootstrapDialog.show({
				            title: '错误提示',
				            message: res.desc
				        });
					}
		        },
		        complete: function () {
		            
		        },
		        error: function (data) {
		        	alert("异常");
		        }
		    });
		},
		
		delClubMember:function(row){
			var data = {
					clubId:row.clubId,
					playerId:row.playerId
			}
			$.ajax({
		        type: "post",
		        url: '/backend/proxy/delClubMember',
		        dataType: "json",
		        contentType: "application/json",
		        data:JSON.stringify(data),
		        beforeSend: function () {
		        	
		        },
		        success: function (res) {
		        	if (res.code == 0) {
		        		BootstrapDialog.show({
		                    title: '成功提示',
		                    message: '删除玩家操作成功',
		                    buttons: [{
		                        label: '确定',
		                        action: function(dialog) {
		                            dialog.close();
		                            $('.table.table-striped:eq(0)').bootstrapTable('refresh');
		                        }
		                    }]
		                });
					}else{
						BootstrapDialog.show({
				            title: '错误提示',
				            message: res.desc
				        });
					}
		        },
		        complete: function () {
		            
		        },
		        error: function (data) {
		        	alert("异常");
		        }
		    });
		}
		
		
}


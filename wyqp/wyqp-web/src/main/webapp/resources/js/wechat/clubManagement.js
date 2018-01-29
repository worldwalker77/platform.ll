$(function() {
	/*bootstrap table*/
	$('.table.table-striped:eq(0)').bootstrapTable({
		method : 'post',
	    url:"/backend/proxy/getProxyClubs",//请求数据url
	    queryParams: function (params) {
	        return {
	            offset: params.offset,  //页码
	            limit: params.limit,   //页面大小
	            clubId : $('#clubId').val(),
	            proxyId : $('#proxyId').val()
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
			field : 'clubName',
			title : '俱乐部名称'
		},{
			field : 'clubOwnerWord',
			title : '老板留言'
		},{
			field : 'status',
			title : '开启审核'
		},
	     {
			field : 'proxyId',
			title : '代理ID'
		},{
			field:'createTime',
			title: '创建时间'
        }, {
			field : '',
			title : '操作',
			formatter:function(value,row,index){  
	            var edit = "<a href='#' onclick='clubManagementUtil.edit(" + JSON.stringify(row) + ")'>编辑</a>";  
	            var del = "<a href='#' onclick='clubManagementUtil.delProxyClub(" + JSON.stringify(row) + ")'>删除</a>";  
                return edit + "&nbsp;|&nbsp;" + del;  
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
	});
	
    $('#addProxyClub').click(function(){
    	$("#editClubName").val('');
		$("#editClubOwnerWord").val('');
		$("#editStatus").val('');
		$("#hiddenClubId").val('0');
    	$('#myModal').modal({});
	});
    
    $('#modifyProxyClub').click(function(){
    	clubManagementUtil.modifyProxyClub();
	});
});

var clubManagementUtil = {
		edit:function(row){
			$("#editClubName").val(row.clubName);
			$("#editClubOwnerWord").val(row.clubOwnerWord);
			$("#editStatus").val(row.status);
			$("#hiddenClubId").val(row.clubId);
			$('#myModal').modal({});
		},
		modifyProxyClub:function(){
			var data = {
					clubName:$("#editClubName").val(),
					clubOwnerWord:$("#editClubOwnerWord").val(),
					status:$("#editStatus").val(),
					clubId:$("#hiddenClubId").val()
			}
			$.ajax({
		        type: "post",
		        url: '/backend/proxy/modifyProxyClub',
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
		                    message: '俱乐部操作成功',
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
		delProxyClub:function(row){
			var data = {
					clubId:row.clubId
			}
			$.ajax({
		        type: "post",
		        url: '/backend/proxy/delProxyClub',
		        dataType: "json",
		        contentType: "application/json",
		        data:JSON.stringify(data),
		        beforeSend: function () {
		        	
		        },
		        success: function (res) {
		        	if (res.code == 0) {
		        		BootstrapDialog.show({
		                    title: '成功提示',
		                    message: '删除俱乐部操作成功',
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


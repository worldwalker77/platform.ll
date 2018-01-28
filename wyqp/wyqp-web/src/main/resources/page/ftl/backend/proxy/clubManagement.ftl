<!DOCTYPE html>
<html>
  <head>
    <title>嘻哈纸牌</title>
    <meta charset="utf-8">
	<#include "ftl/common/common.ftl"/> 
  </head>
    
  <body  style="overflow:auto">
		<div id="crash-container" class="container-fluid"  style="padding-left:0;padding-right:0;padding-buttom:0">
		<div class="panel panel-default">
			<div class="panel-body">
					<div class="row" style="margin-top:5px"> 
						<div class="col-xs-4">
							<span>代理ID </span>
						</div>
						<div class="col-xs-1">
							<input type="text" id="proxyId">
						</div>
						<div class="pull-right">
							<div class="col-xs-1">
								<button type="reset" class="btn btn-sm btn-primary" id="resetBtn">
									<i></i>清空
								</button>
							</div>
						</div>
					</div>
					
					<div class="row" style="margin-top:5px"> 
						<div class="col-xs-4">
							<span>俱乐部ID </span>
						</div>
						<div class="col-xs-1">
							<input type="text" id="clubId">
						</div>
						<div class="pull-right">
							<div class="col-xs-1">
								<button type="button" class="btn btn-sm btn-primary " id="searchBtn">
									<i></i>查询
								</button>
							</div>
						</div>
					</div>
					
					<div class="row" style="margin-top:5px"> 
						<div class="col-xs-4">
							<button id='addProxyClub' type="button">新增俱乐部</button>
						</div>
					</div>
				</div>
			</div>
		</div>
    	<!-- table 插件 -->
		<#include "ftl/common/table.ftl"/>
		<!-- 模态框（Modal） -->
		<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		    <div class="modal-dialog">
		        <div class="modal-content">
		            <div class="modal-header">
		                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		                <h4 class="modal-title" id="myModalLabel">新增俱乐部</h4>
		            </div>
		            <div class="modal-body">
		            		<input type="hidden" id="hiddenClubId">
		            		<div class="row"> 
			            		<div class="col-xs-3">
									<span>俱乐部名称</span>
								</div>
								<div class="col-xs-4">
									<input size="16" type="text" id="editClubName">
								</div>
							</div>
							<div class="row"> 
			            		<div class="col-xs-3">
									<span>老板留言</span>
								</div>
								<div class="col-xs-4">
									<input size="16" type="text" id="editClubOwnerWord">
								</div>
							</div>
							<div class="row"> 
			            		<div class="col-xs-3">
									<span>是否开启审核(1:开启 0:不开启)</span>
								</div>
								<div class="col-xs-4">
									<input size="16" type="text" id="editStatus">
								</div>
							</div>
		            </div>
		            <div class="modal-footer">
		                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
		                <button type="button" class="btn btn-primary" id="modifyProxyClub">确定</button>
		            </div>
		        </div><!-- /.modal-content -->
		    </div><!-- /.modal -->
	<script type="text/javascript" src="/resources/js/wechat/clubManagement.js?version=${version}"></script>
  </body>
</html>


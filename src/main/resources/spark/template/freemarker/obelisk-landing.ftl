<#assign content>
<div class="container">
    <nav class="navbar navbar-inverse navbar-fixed-top" id="landingNav">
      <div class="container-fluid">
        <div class="navbar-header">
          <a class="navbar-brand" href="">Obelisk</a>
        </div>
        <ul class="nav navbar-nav navbar-right">
          <button class="btn btn-success navbar-btn" data-toggle="modal" data-target="#myModal">Create Game</button>
        </ul>
      </div>
    </nav>

    <div id="myModal" class="modal fade" role="dialog">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Create a game with up to 4 total players!</h4>
                </div>
                <div class="modal-body">
                    <#include "create-game-form.ftl">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" id="createGameButton" data-dismiss="modal">Create Game</button>
                </div>
            </div>
        </div>
    </div>

    <div class="panel-group" id="accordion">
        <#--<div id = id  class = 'panel panel-default'>-->
            <#--<div class='panel-heading'>-->
                <#--<h4 class='panel-title'>-->
                    <#--<a data-toggle='collapse' data-parent='#accordion' href='#collapse0'>-->
                        <#--Game Name-->
                    <#--</a>-->
                <#--</h4>-->
            <#--</div>-->
            <#--<div id='collapse0' class='panel-collapse collapse'>-->
                <#--<div class='panel-body'>-->
                    <#--<div>Available Spots: 1</div>-->
                    <#--<div>Human Players: 2</div>-->
                    <#--<div>AI Players: 1</div>-->
                    <#--<button class='btn btn-info btn-xs' id='join0' onClick='joinGame(" + newGame.id + ")' >Join Game</button>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div>-->
    </div>

    <#--<div class="images">-->
        <#--<img src="sprites/portal.png">-->
    <#--</div>-->
</div>


<#include "waiting-page.ftl">

</#assign>
<#include "main-landing.ftl">


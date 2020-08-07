<#assign content>



<div id="quarry">
    <h2 class="quarryHeader">Quarry</h2>
    <table class="table">
        <thead>
        <tr class="quarryHeader">
            <th>Demon</th>
            <th>Count</th>
        </tr>
        </thead>
        <tbody>
        <tr class="red">
            <td>Red</td>
            <td id="redDemonCount"></td>
        </tr>
        <tr class="yello">
            <td>Yellow</td>
            <td id="yellowDemonCount"></td>
        </tr>
        <tr class="purp">
            <td>Purple</td>
            <td id="purpleDemonCount"></td>
        </tr>
        </tbody>
    </table>

    <#include "capturable-demons.ftl">

    <div id = "button_div">

        <button type="button" id="mineDemonsButton" class="btn btn-med btn-success" data-toggle="tooltip" data-placement="bottom" title="mine">Mine Demons</button>

        <button type="button" id="placeObeliskButton" class="btn btn-med btn-success" data-toggle="tooltip" data-placement="bottom" title="place">Place Obelisk</button>

        <button type="button" id="reinforceObeliskButton" class="btn btn-med btn-success" data-toggle="tooltip" data-placement="bottom" title="reinforce">Reinforce Obelisk</button>

        <button type="button" id="rotateTileButton" class="btn btn-med btn-success" data-toggle="tooltip" data-placement="bottom" title="rotate">Rotate Tile</button>

    </div>



    <div class="placeObeliskForm" style="display:none">
        <p>Click on a black spot on the board to place an obelisk.</p>
        <#--<div class="form-group">-->
            <#--<label for="obeliskRow">Row:</label>-->
            <#--<input type="obeliskRow" class="form-control" id="obeliskRow">-->
        <#--</div>-->
        <#--<div class="form-group">-->
            <#--<label for="obeliskCol">Column:</label>-->
            <#--<input type="obeliskCol" class="form-control" id="obeliskCol">-->
        <#--</div>-->
        <#--<button type="submit" class="btn btn-default" id="placeObeliskSubmit">Place</button>-->
    </div>

    <div class="rotateTileForm" style="display:none">
        <#--<p>Enter the number of rotations then click the tile to rotate</p>-->
        <button type="submit" class="btn btn-info btn-sm" id="startRotate">Start Rotate</button>
        <button type="submit" class="btn btn-info btn-sm" id="endRotate">End Rotate</button>
        <#--<div class="form-group">-->
            <#--<label for="numRots">Number of clockwise turns:</label>-->
            <#--<input type="numRots" class="form-control" id="numRots">-->
        <#--</div>-->
    </div>

    <div class="reinforceObeliskForm" style="display:none">
        <small class="form-text text-muted">
            Choose to reinforce your obelisk using either an obelisk or resources, then click the obelisk you'd like to reinforce.
        </small>
        <div class="form-check">
            <input class="form-check-input" type="radio" name="reinforceObeliskType" id="reinforceWithObeliskButton" value="obelisk" data-toggle="popover" data-trigger="focus" data-placement="left" data-content="Out of obelisks :(">
            <label class="form-check-label" for="reinforceWithObeliskButton">Reinforce with obelisk</label>
        </div>
        <div class="form-check">
            <input class="form-check-input" type="radio" name="reinforceObeliskType" id="reinforceWithResourcesButton" value="resources">
            <label class="form-check-label" for="reinforceWithResourcesButton">Reinforce with resources</label>

            <div class="resourcesCombosForm" style="display:none">
                <small class="form-text text-muted">
                    Choose the combination of resources to use to reinforce.
                </small>
                <div id="resourcesComboForm">
                </div>
                <button type="submit" class="btn btn-xs btn-default btn-light" id="resourcesComboSubmit">Reinforce</button>
            </div>
        </div>
    </div>

    <div class="well" id="movesLeftText"></div>
    <button type="button" id="endTurnButton" class="btn btn-med btn-primary">End Turn</button>
</div>


<nav class="navbar navbar-inverse navbar-fixed-bottom" id = "navigation-bar"></nav>


</#assign>
    <#include "main-game.ftl">


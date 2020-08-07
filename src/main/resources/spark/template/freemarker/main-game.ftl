<!DOCTYPE html>
<head>
    <meta charset="utf-8">
    <title>${title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- In real-world webapps, css is usually minified and
         concatenated. Here, separate normalize from our code, and
         avoid minification for clarity. -->

    <link rel="stylesheet"
          href="<#if cssPath?has_content>${cssPath}</#if>css/bootstrap.css">
    <link rel="stylesheet"
          href="<#if cssPath?has_content>${cssPath}</#if>css/bootstrap-theme.css">
    <link rel="stylesheet"
          href="<#if cssPath?has_content>${cssPath}</#if>css/normalize.css">
    <link rel="stylesheet"
          href="<#if cssPath?has_content>${cssPath}</#if>css/html5bp.css">
    <link rel="stylesheet"
          href="<#if cssPath?has_content>${cssPath}</#if>css/main.css">


    <link href="https://fonts.googleapis.com/css?family=Poppins"
          rel="stylesheet">

    <#if additionalCSS?has_content>
        ${additionalCSS}
    </#if>

</head>
<body>
${content}
<!-- Again, we're serving up the unminified source for clarity. -->
<script src="<#if cssPath?has_content>${cssPath}</#if>js/jquery-2.1.1.js"></script>

<#if additionalScripts?has_content>
    ${additionalScripts}
</#if>

<script src="<#if cssPath?has_content>${cssPath}</#if>js/jquery-3.1.1.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/bootstrap.min.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/sprite.js-master/sprite.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/sprite.js-master/lib/collision.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/message-enums.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/pieces.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/game.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/board.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/moves.js"></script>
<script src="<#if cssPath?has_content>${cssPath}</#if>js/night.js"></script>

</body>
<!-- See http://html5boilerplate.com/ for a good place to start
     dealing with real world issues like old browsers.  -->
</html>

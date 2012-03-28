CS = {};
CS.window = window.frames[0];
CS.document = CS.window.document;
CS.Ext = CS.window.Ext;
CS.search = CS.Ext.getDom("search");
CS.toolbar = CS.search.getElementsByClassName("x-toolbar")[0];
CS.searchbutton = CS.toolbar.getElementsByTagName("button")[0];
CS.searchelement = CS.Ext.get(CS.searchbutton.id);
function replaceSearch(evt, el, o) {
  evt.stopPropagation();
  alert("Personal Search!");
}
CS.searchelement.addListener("click", replaceSearch);
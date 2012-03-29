function setup() {
    CS = {};
    CS.search = Ext.getDom("search");
    if (CS.search == null) return; // wrong frame
    CS.toolbar = CS.search.getElementsByClassName("x-toolbar")[0];
    CS.searchfield = CS.toolbar.getElementsByTagName("input")[0];
    CS.searchbutton = CS.toolbar.getElementsByTagName("button")[0];
    CS.searchelement = Ext.get(CS.searchbutton.id);
    function search(evt, el, o) {
	evt.stopPropagation();
	Ext.Ajax.request({
	    url: 'http://localhost:8000',
	    params: {
		id: 1,
		query: CS.searchfield.textContent
	    },
	    success: function(response){
		var text = response.responseText;
	    }
	});
    }
    CS.searchelement.addListener("click", search);
    CS.searchbutton.textContent = "SuperSearch";
}
setTimeout("setup();", 10000);
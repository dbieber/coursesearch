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
	    url: 'http://localhost:8000/',
	    params: {
		query: CS.searchfield.value
	    },
	    success: function(response){
		var text = response.responseText;
		console.log(text);
	    }
	});
    }
    CS.searchelement.addListener("click", search);
    CS.searchbutton.textContent = "SuperSearch";
}
setTimeout("setup();", 10000);
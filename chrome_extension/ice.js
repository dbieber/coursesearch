function setup() {
    CS = {};
    CS.window = window.frames[0];
    CS.search = Ext.getDom("search");
    CS.toolbar = CS.search.getElementsByClassName("x-toolbar")[0];
    CS.searchbutton = CS.toolbar.getElementsByTagName("button")[0];
    CS.searchelement = Ext.get(CS.searchbutton.id);
    function search(evt, el, o) {
	evt.stopPropagation();
	Ext.Ajax.request({
	    url: 'http://google.com',
	    params: {
		id: 1
	    },
	    success: function(response){
		var text = response.responseText;
		alert(text);
	    }
	});
    }
    CS.searchelement.addListener("click", search);
    CS.searchbutton.textContent = "SuperSearch";
}
setTimeout("setup();", 10000);
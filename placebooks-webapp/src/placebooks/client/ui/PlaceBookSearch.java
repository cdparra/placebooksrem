package placebooks.client.ui;

import placebooks.client.AbstractCallback;
import placebooks.client.PlaceBookService;
import placebooks.client.model.PlaceBookEntry;
import placebooks.client.model.Shelf;
import placebooks.client.ui.places.PlaceBookSearchPlace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PlaceBookSearch extends Composite
{
	interface PlaceBookSearchUiBinder extends UiBinder<Widget, PlaceBookSearch>
	{
	}

	private static PlaceBookSearchUiBinder uiBinder = GWT.create(PlaceBookSearchUiBinder.class);

	@UiField
	Panel placebooks;

	@UiField
	PlaceBookToolbar toolbar;
	
	@UiField
	TextBox search;
	
	private Shelf shelf;

	public PlaceBookSearch(final String searchString, final PlaceController placeController, final Shelf shelf)
	{
		initWidget(uiBinder.createAndBindUi(this));

		Window.setTitle("PlaceBooks Search - " + searchString);

		search.setText(searchString);
		
		toolbar.setPlaceController(placeController);
		toolbar.setShelf(shelf);
		GWT.log("Search: " + searchString);
		setShelf(shelf);
		
		PlaceBookService.search(searchString, new AbstractCallback()
		{
			@Override
			public void success(Request request, Response response)
			{
				setShelf(Shelf.parse(response.getText()));				
			}
		});
		
		RootPanel.get().getElement().getStyle().clearOverflow();				
	}

	private void setShelf(final Shelf shelf)
	{
		if(this.shelf != shelf)
		{
			this.shelf = shelf;
			placebooks.clear();
			if (shelf != null)
			{
				for (final PlaceBookEntry entry : shelf.getEntries())
				{
					placebooks.add(new PlaceBookEntryWidget(toolbar, entry));
				}
			}
		}
	}
	
	@UiHandler("searchButton")
	void handleSearch(final ClickEvent event)
	{
		search();
	}

	@UiHandler("search")
	void handleSearchEnter(final KeyPressEvent event)
	{
		if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode())
		{
			search();
		}
	}

	private void search()
	{
		toolbar.getPlaceController().goTo(new PlaceBookSearchPlace(search.getText(), toolbar.getShelf()));
	}	
}
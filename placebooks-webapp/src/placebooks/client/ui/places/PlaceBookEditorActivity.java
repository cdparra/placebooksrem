package placebooks.client.ui.places;

import placebooks.client.AbstractCallback;
import placebooks.client.PlaceBookService;
import placebooks.client.model.PlaceBook;
import placebooks.client.ui.PlaceBookEditor;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class PlaceBookEditorActivity extends PlaceBookActivity
{
	private final String key;
	private final PlaceBook placebook;
	private final PlaceController placeController;

	public PlaceBookEditorActivity(final PlaceController controller, final PlaceBookEditorPlace place)
	{
		super(place.getShelf());
		this.key = place.getKey();
		this.placebook = place.getPlaceBook();
		this.placeController = controller;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus)
	{
		final PlaceBookEditor editor = new PlaceBookEditor(placeController, shelf);
		if (placebook != null)
		{
			editor.setPlaceBook(placebook);
		}
		else
		{
			PlaceBookService.getPlaceBook(key, new AbstractCallback()
			{
				@Override
				public void success(final Request request, final Response response)
				{
					final PlaceBook placebook = PlaceBook.parse(response.getText());
					editor.setPlaceBook(placebook);
				}
			});
		}

		panel.setWidget(editor);
	}
}

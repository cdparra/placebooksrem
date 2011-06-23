package placebooks.client.ui.places;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;

public class PlaceBookActivityMapper implements ActivityMapper
{
	private final PlaceController controller;

	public PlaceBookActivityMapper(final PlaceController controller)
	{
		super();
		this.controller = controller;
	}

	@Override
	public Activity getActivity(final Place place)
	{
		if (place instanceof PlaceBookEditorPlace)
		{
			return new PlaceBookEditorActivity(controller, ((PlaceBookEditorPlace) place).getPlaceBook(),
					((PlaceBookEditorPlace) place).getKey());
		}
		else if (place instanceof PlaceBookHomePlace)
		{
			return new PlaceBookHomeActivity(controller);
		}
		else if (place instanceof PlaceBookEditorNewPlace)
		{
			return new PlaceBookEditorActivity(controller, ((PlaceBookEditorNewPlace) place).getPlaceBook(), null);
		}
		else if (place instanceof PlaceBookPreviewPlace) { return new PlaceBookPreviewActivity(controller,
				((PlaceBookPreviewPlace) place).getPlaceBook(), ((PlaceBookPreviewPlace) place).getKey()); }
		return null;
	}

}

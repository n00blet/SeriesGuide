package com.battlelancer.seriesguide.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.ui.PeopleActivity;
import com.battlelancer.seriesguide.ui.PersonFragment;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.Credits;
import com.uwetrottmann.tmdb2.entities.CrewMember;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Helps load a fixed number of people into a static layout.
 */
public class PeopleListHelper {

    public static void populateShowCast(Activity activity,
            ViewGroup peopleContainer, Credits credits, String logCategory) {
        populateCast(activity, peopleContainer, credits, PeopleActivity.MediaType.SHOW,
                logCategory);
    }

    public static void populateShowCrew(Activity activity,
            ViewGroup peopleContainer, Credits credits, String logCategory) {
        populateCrew(activity, peopleContainer, credits, PeopleActivity.MediaType.SHOW,
                logCategory);
    }

    public static void populateMovieCast(Activity activity,
            ViewGroup peopleContainer, Credits credits, String logCategory) {
        populateCast(activity, peopleContainer, credits, PeopleActivity.MediaType.MOVIE,
                logCategory);
    }

    public static void populateMovieCrew(Activity activity,
            ViewGroup peopleContainer, Credits credits, String logCategory) {
        populateCrew(activity, peopleContainer, credits, PeopleActivity.MediaType.MOVIE,
                logCategory);
    }

    /**
     * Add views for at most three cast members to the given {@link android.view.ViewGroup} and a
     * "Show all" link if there are more.
     */
    private static void populateCast(Activity activity, ViewGroup peopleContainer, Credits credits,
            PeopleActivity.MediaType mediaType, String logCategory) {
        if (peopleContainer == null) {
            // nothing we can do, view is already gone
            Timber.d("populateCast: container reference gone, aborting");
            return;
        }

        peopleContainer.removeAllViews();

        // show at most 3 cast members
        LayoutInflater inflater = LayoutInflater.from(peopleContainer.getContext());
        List<CastMember> cast = credits.cast;
        for (int i = 0; i < Math.min(3, cast.size()); i++) {
            CastMember castMember = cast.get(i);

            View personView = createPersonView(activity, inflater, peopleContainer, castMember.name,
                    castMember.character, castMember.profile_path);
            personView.setOnClickListener(
                    new OnPersonClickListener(activity, mediaType, credits.id,
                            PeopleActivity.PeopleType.CAST, castMember.id, logCategory)
            );

            peopleContainer.addView(personView);
        }

        if (cast.size() > 3) {
            addShowAllView(inflater, peopleContainer,
                    new OnPersonClickListener(activity, mediaType, credits.id,
                            PeopleActivity.PeopleType.CAST, logCategory)
            );
        }
    }

    /**
     * Add views for at most three crew members to the given {@link android.view.ViewGroup} and a
     * "Show all" link if there are more.
     */
    private static void populateCrew(Activity activity, ViewGroup peopleContainer, Credits credits,
            PeopleActivity.MediaType mediaType, String logCategory) {
        if (peopleContainer == null) {
            // nothing we can do, view is already gone
            Timber.d("populateCrew: container reference gone, aborting");
            return;
        }

        peopleContainer.removeAllViews();

        // show at most 3 crew members
        LayoutInflater inflater = LayoutInflater.from(peopleContainer.getContext());
        List<CrewMember> crew = credits.crew;
        for (int i = 0; i < Math.min(3, crew.size()); i++) {
            CrewMember castMember = crew.get(i);

            View personView = createPersonView(activity, inflater, peopleContainer, castMember.name,
                    castMember.job, castMember.profile_path);
            personView.setOnClickListener(
                    new OnPersonClickListener(activity, mediaType, credits.id,
                            PeopleActivity.PeopleType.CREW, castMember.id, logCategory)
            );

            peopleContainer.addView(personView);
        }

        if (crew.size() > 3) {
            addShowAllView(inflater, peopleContainer,
                    new OnPersonClickListener(activity, mediaType, credits.id,
                            PeopleActivity.PeopleType.CREW, logCategory)
            );
        }
    }

    private static View createPersonView(Context context, LayoutInflater inflater,
            ViewGroup peopleContainer, String name, String description, String profilePath) {
        View personView = inflater.inflate(R.layout.item_person, peopleContainer, false);

        // use clickable instead of activatable background
        personView.setBackgroundResource(
                Utils.resolveAttributeToResourceId(peopleContainer.getContext().getTheme(),
                        R.attr.selectableItemBackground));
        // support keyboard nav
        personView.setFocusable(true);

        ServiceUtils.loadWithPicasso(context, TmdbTools.buildProfileImageUrl(context, profilePath,
                TmdbTools.ProfileImageSize.W185))
                .resizeDimen(R.dimen.person_headshot_size, R.dimen.person_headshot_size)
                .centerCrop()
                .error(R.color.protection_dark)
                .into((ImageView) personView.findViewById(R.id.imageViewPerson));

        TextView nameView = personView.findViewById(R.id.textViewPerson);
        nameView.setText(name);

        TextView descriptionView = personView.findViewById(R.id.textViewPersonDescription);
        descriptionView.setText(description);

        return personView;
    }

    private static class OnPersonClickListener implements View.OnClickListener {

        private final Activity activity;
        private final int itemTmdbId;
        private final int personTmdbId;
        private final PeopleActivity.PeopleType peopleType;
        private final PeopleActivity.MediaType mediaType;
        private final String logCategory;

        /**
         * Listener that will show cast or crew members for the given TMDb entity.
         */
        public OnPersonClickListener(Activity activity, PeopleActivity.MediaType mediaType,
                int mediaTmdbId, PeopleActivity.PeopleType peopleType, String logCategory) {
            this(activity, mediaType, mediaTmdbId, peopleType, -1, logCategory);
        }

        /**
         * Listener that will show cast or crew members for the given TMDb entity and pre-selects a
         * specific cast or crew member.
         */
        public OnPersonClickListener(Activity activity, PeopleActivity.MediaType mediaType,
                int mediaTmdbId, PeopleActivity.PeopleType peopleType, int personTmdbId,
                String logCategory) {
            this.activity = activity;
            this.itemTmdbId = mediaTmdbId;
            this.peopleType = peopleType;
            this.mediaType = mediaType;
            this.personTmdbId = personTmdbId;
            this.logCategory = logCategory;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), PeopleActivity.class);
            i.putExtra(PeopleActivity.InitBundle.ITEM_TMDB_ID, itemTmdbId);
            i.putExtra(PeopleActivity.InitBundle.PEOPLE_TYPE, peopleType.toString());
            i.putExtra(PeopleActivity.InitBundle.MEDIA_TYPE, mediaType.toString());
            if (personTmdbId != -1) {
                // showing a specific person
                i.putExtra(PersonFragment.InitBundle.PERSON_TMDB_ID, personTmdbId);
            }
            Utils.startActivityWithAnimation(activity, i, v);
            Utils.trackAction(v.getContext(), logCategory, "Cast or crew");
        }
    }

    private static void addShowAllView(LayoutInflater inflater, ViewGroup peopleContainer,
            View.OnClickListener clickListener) {
        TextView showAllView = (TextView) inflater.inflate(R.layout.item_action_add,
                peopleContainer, false);
        showAllView.setText(R.string.action_display_all);
        showAllView.setOnClickListener(clickListener);
        peopleContainer.addView(showAllView);
    }

    public static List<Person> transformCastToPersonList(List<CastMember> cast) {
        List<Person> people = new ArrayList<>();
        for (CastMember castMember : cast) {
            Person person = new Person();
            person.tmdbId = castMember.id;
            person.name = castMember.name;
            person.description = castMember.character;
            person.profilePath = castMember.profile_path;
            people.add(person);
        }
        return people;
    }

    public static List<Person> transformCrewToPersonList(List<CrewMember> crew) {
        List<Person> people = new ArrayList<>();
        for (CrewMember crewMember : crew) {
            Person person = new Person();
            person.tmdbId = crewMember.id;
            person.name = crewMember.name;
            person.description = crewMember.job;
            person.profilePath = crewMember.profile_path;
            people.add(person);
        }
        return people;
    }

    public static class Person {
        public int tmdbId;
        public String name;
        public String description;
        public String profilePath;
    }
}

# Popular Movies Application

This project is an exercise as part of the **Android Developer Nanodegree**, by **Udacity**. It allows users to discover the most popular movies playing. This is the stage 2 of the app.

The application queries an external database that has the most popular and top rated movies. It provides a grid arrangement of movie posters, and shows details of the movies when the user taps in that poster. 🎥

The users can read reviews of a selected movie, and mark movie as favorite in the details view. This stores the movie in a local database.

The program uses:

a) **The Movie DB API** for querying `themoviedb.org` database, about the movie data  (<https://developers.themoviedb.org>).

b) **Picasso** library for handling image loading and caching (<http://square.github.io/picasso/).

c) **SQLite**  for the database engine (<https://www.sqlite.org/index.html>).


# Installation

* Download the all the files and subdirectories and import the project in the **Android Studio**.

* Obtain an `API-key` from **The Movie DB** (<https://developers.themoviedb.org>) and set the value of `API_KEY` inside the `PopularMoviesPreferences` class. There is another more secure way for handling the `API-key`, storing it inside a **new** `gradle.properties` file (this is not the usual one), under the `.gradle` directory. The `git` will not add that file to the staging index (confirm by seeing `.gitignore`). It will not be committed and pushed into a public repository, so protecting the key. Please, see the instructions in the link <https://technobells.com/best-way-to-store-your-api-keys-for-your-android-studio-project-e4b5e8bb7d23>. (As always, first make an experience with a fake key, for confirming that it will not be in your public repository.)

* Turn on the developer mode of your **Android** phone or tablet, and connect to the **Android Studio** <https://developer.android.com/studio/index.html>.

* Configure the drivers of the `USB` port of your computer, so your **Android** device will be able to communicate with the computer. See orientations on the **Android Studio** sites <https://developer.android.com/studio/run/oem-usb.html>.

* In **Android Studio**, select to run your app. It will install the app in your connected device and starts it. See instructions on <https://developer.android.com/training/basics/firstapp/running-app.html>.


# Common usage

Well, do you like movies?

**When the app loads**: When the app loads, it tries to load data (movie information), of the most popular movies, and shows the grid with the posters.

**Selecting 'popular', 'top rated', or 'favorites'**: In the menu, just select the `popular`, `top rated`, or 'favorites' options, and the app will reload the data with the choice. In case of `favorites`, it will query the local database, about the movie information and the reviews.

**Viewing movie details**: Just tap on the movie poster!😮

**Deleting movies from `favorites`**: In the main view, while seeing the `favorites` list, touch the movie's poster and scroll right or left. It automatically will be deleted from the local database.

**Viewing trailers and reviews**: Just tap on the `Trailers` or `Reviews` in the `details` view! 📣

That's it!


* This app is for learning purposes. 📚


# Credits

These are some useful links, in addition to **Udacity** itself, that were queried in this project:

https://developer.android.com/guide/index.html

https://developers.themoviedb.org/3/getting-started/introduction

https://stackoverflow.com/questions/5082122/passing-jsonobject-into-another-activity

https://stackoverflow.com/questions/27117687/passing-jsonobject-to-another-activity-via-intent/27119267

https://stackoverflow.com/questions/42640827/trying-to-load-an-image-using-picasso-library-cant-get-the-context

https://stackoverflow.com/questions/36514887/layoutmanager-for-recyclerview-grid-with-different-cell-width

https://stackoverflow.com/questions/21889735/resize-image-to-full-width-and-variable-height-with-picasso/26782046

https://stackoverflow.com/questions/5015844/parsing-json-object-in-java

https://stackoverflow.com/questions/6150080/set-a-menu-item-as-checked-from-code

https://futurestud.io/tutorials/picasso-placeholders-errors-and-fading

https://www.youtube.com/watch?v=ocWtozJaFz0

https://technobells.com/best-way-to-store-your-api-keys-for-your-android-studio-project-e4b5e8bb7d23

https://android--code.blogspot.com.br/2015/08/android-listview-itemrow-height.html

https://stackoverflow.com/questions/4119441/how-to-scroll-to-top-of-long-scrollview-layout

https://stackoverflow.com/questions/2736389/how-to-pass-an-object-from-one-activity-to-another-on-android

https://stackoverflow.com/questions/13070791/android-cursor-to-jsonarray

https://stackoverflow.com/questions/3572463/what-is-context-on-android?rq=1

https://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database

https://stackoverflow.com/questions/23078041/how-to-retrieve-an-image-from-an-imageview-and-store-it-in-an-sqlite-database-in

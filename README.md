## What is the name of your web application?

Studbuds

## Do we have a clear understanding of the problem?

Many students study by rereading notes or flipping flashcards which does not improve long term retention. While flashcards are still commonly used most applications don't enforce active recall or provide meaningful feedback which leads to inefficient studying. 

Additionally, sometimes students want to study with others, but aren’t comfortable reaching out. This application allows these students to find others who are in a similar situation and group up. 

## How is this problem solved currently (if at all)?

Right now this problem is addressed by tools such as quizlet. Quizlet offers an easy to use interface but often encourages passive studying and locks its more rewarding features behind paywalls.

## How will this project make life better? Is it educational or just for entertainment?

This project is mainly educational. We aim to improve the studying experience by encouraging active recall through a quiz that requires you to answer before seeing the correct response. It also has features such as performance tracking helping users focus on weaker areas.  The group study feature adds a collaborative and engaging side to learning.

## Who is the target audience? Who will use your app or play your game?

The target audience is mainly toward all types of  students and learners who are willing to build consistent study habits through quizzes and flashcards. The app is designed for the individuals seeking designated learning, progress tracking, and motivation through streaks and activity statistics. This is especially suitable for students, self-learners and anyone who is interested in improving their discipline and monitoring their learning patterns over time.

## What is the scope of your project?

To create a web application that allows users to register, login, create, manage, and study flashcard sets. The application will include multiple modes such as quiz mode and time challenge mode along with features like performance tracking, streaks, and group study. All user data, flashcards , and stats will be stored in a database and the system will be designed to support real-time interaction in group settings.  The application will also use an external api for account registration and authentication

## Does this project have many individual features or one main feature (a possibility with many subproblems)? These are the ‘epics’ of your project.

This project has one main flashcard feature and other individual features such as time challenge mode, group study mode and stats display.

## What are the epics? For example, as a regular user of your site, what general actions/features can I perform? You may choose to start creating some UI mockups on paper. This may help determine your initial features.

**Account registration**<br>
Description: Account registration enables a user to create an account through an API. The
system accepts credentials, validates them, and stores them to a database, along with the
account type (user or admin). After an account is created, the system allows users to enter their
credentials to log into the account to access the application.<br><br>
User story: as a new user, I want to register an account through an API so that I can access the
application and save/interact with my personal data.

**Flashcards**<br>
Description: Main activity to achieve the purpose of this application. Unlike traditional flashcards,
it will feature multiple modes including but not limited to fill-in-blanks, multiple-choice, matching
cards, and regular flashcards where the concept is on the front of the card and the solution on
the back. The flashcard sets can contain subsets (i.e. sections) and the sets will contain
material/concepts that the user wishes to learn. The ownership of each flashcard set is tied to
the account of the user that created it and is stored in a database for future visits to the
application. Users can create, edit, and delete their flashcard sets.<br><br>
User story: As a user, I want to create and manage my flashcard sets to organize material I want
to study
User story: As a user, I want multiple flashcard modes (e.g. fill in blanks, matching cards) so that learning is more engaging and effective

**Levelling system**<br>
Description: The system tracks user interactions (flashcard set completion, daily log in, etc.) and
awards them XP accordingly. This XP is used to show user progress and engagement with the
app. As users level up, they will unlock achievements and badges next to their name. Levels,
XP, achievements, and badges will all be stored in a database associated with the user.<br><br>
User story: As a user, I want to earn XP when I complete flashcard sets and log in daily so that I
can track my progress and unlock achievements and badges.

**Streaks**<br>
Description: Represent continuous learning achievements. The streak count increases when the
user completes activities on consecutive days. To maintain or increase a streak, the user must
complete a specified number of quizzes or flashcards each day. When the requirement is met,
an animation is displayed and the streak increments by +1.<br><br>
User Story: As a user, I want to maintain a continuous learning streak by completing quizzes or
flashcards each day so that I can build discipline, track my consistency, and see my streak
increase with animation feedback.

**Lifetime Stats**<br>
Description: Function similarly to GitHub activity statistics. They display the days on which the
user used the app most frequently, based on the number of completed quizzes. Statistics are
organized by week, with monthly views planned for future updates. Lifetime stats are publicly
visible to other users.<br><br>
User Story: As a user, I want to view my lifetime activity statistics so that I can understand my
learning patterns, identify which days I was most active over time, and share my progress
publicly with others.

**Time challenge mode**<br>
Description: Time Challenge mode allows users to study flashcards under a time constraint to
increase focus and engagement. Users select a flashcard set and are given a certain time to
answer as many cards as possible. Time Challenge mode tracks correct and incorrect answers,
calculates a score based on speed and accuracy.<br><br>
User Story: As a user I want to complete flashcards within a time constraint so that i can quiz
myself, improve recall speed and make studying more engaging.

**Group Study**<br>
Description: Group study allows users to study & connect with others. The group leader creates
a group that enables other users to join via invite, allowing them to create a group flashcard set,
and compete together in Time Challenge Mode, etc. Can level up as a group/have a group log
in streak(?). The group persists until it is deleted and users can communicate with each other
through a built-in text chat.<br><br>
User Story: As a user, I want to be able to study more effectively with other students. At school, I
notice everyone studies alone and I think we could all benefit if we collaborate together. We can
compete for the fastest Time Challenge Mode time, incentivising us to study more. We can level
up our stats on the website while improving our knowledge on course topics

## Is the amount of work required in this proposal sufficient for five group members? A rough rule of thumb is that each group member should have one major feature.
Yes, the amount of work required in this proposal is sufficient for five group members as we decide who is going to work on what part, and each group member has at least one major feature to accomplish.


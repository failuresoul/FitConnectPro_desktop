-- ADMIN TABLES

CREATE TABLE Admins (
                        admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        phone TEXT,
                        role TEXT NOT NULL,
                        account_status TEXT DEFAULT 'ACTIVE',
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        last_login DATETIME
);

CREATE TABLE Admin_Logs (
                            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            admin_id INTEGER NOT NULL,
                            action_type TEXT NOT NULL,
                            target_table TEXT,
                            target_id INTEGER,
                            action_details TEXT,
                            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (admin_id) REFERENCES Admins(admin_id)
);

-- TRAINER TABLES

CREATE TABLE Trainers (
                          trainer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                          username TEXT UNIQUE NOT NULL,
                          password_hash TEXT NOT NULL,
                          full_name TEXT NOT NULL,
                          email TEXT UNIQUE NOT NULL,
                          phone TEXT,
                          specializations TEXT,
                          experience_years INTEGER DEFAULT 0,
                          certifications TEXT,
                          max_clients INTEGER DEFAULT 10,
                          current_clients INTEGER DEFAULT 0,
                          account_status TEXT DEFAULT 'ACTIVE',
                          salary REAL DEFAULT 0.0,
                          hired_by_admin_id INTEGER,
                          hire_date TEXT,
                          last_login TEXT,
                          FOREIGN KEY (hired_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Applications (
                                      application_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      full_name TEXT NOT NULL,
                                      email TEXT NOT NULL,
                                      phone TEXT,
                                      age INTEGER,
                                      education TEXT,
                                      certifications TEXT,
                                      experience_years INTEGER,
                                      specializations TEXT,
                                      cover_letter TEXT,
                                      application_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      status TEXT NOT NULL,
                                      reviewed_by_admin_id INTEGER,
                                      review_date DATETIME,
                                      FOREIGN KEY (reviewed_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Member_Assignment (
                                           assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           trainer_id INTEGER NOT NULL,
                                           member_id INTEGER NOT NULL,
                                           assignment_date DATE NOT NULL,
                                           end_date DATE,
                                           status TEXT NOT NULL,
                                           assigned_by_admin_id INTEGER,
                                           FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                           FOREIGN KEY (member_id) REFERENCES Members(member_id),
                                           FOREIGN KEY (assigned_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Workout_Plans (
                                       plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       trainer_id INTEGER NOT NULL,
                                       member_id INTEGER NOT NULL,
                                       plan_date DATE NOT NULL,
                                       focus_area TEXT,
                                       total_duration INTEGER,
                                       expected_calories INTEGER,
                                       instructions TEXT,
                                       created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       status TEXT NOT NULL,
                                       FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Trainer_Plan_Exercises (
                                        plan_exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        plan_id INTEGER NOT NULL,
                                        exercise_id INTEGER NOT NULL,
                                        sets INTEGER,
                                        reps INTEGER,
                                        weight REAL,
                                        rest_seconds INTEGER,
                                        trainer_notes TEXT,
                                        order_number INTEGER,
                                        FOREIGN KEY (plan_id) REFERENCES Trainer_Workout_Plans(plan_id),
                                        FOREIGN KEY (exercise_id) REFERENCES Exercises_Library(exercise_id)
);

CREATE TABLE Trainer_Daily_Goals (
                                     trainer_goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     trainer_id INTEGER NOT NULL,
                                     member_id INTEGER NOT NULL,
                                     goal_date DATE NOT NULL,
                                     workout_duration INTEGER,
                                     calorie_target INTEGER,
                                     water_intake_ml INTEGER,
                                     calorie_limit INTEGER,
                                     protein_target INTEGER,
                                     carbs_target INTEGER,
                                     fats_target INTEGER,
                                     special_instructions TEXT,
                                     created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                     FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Trainer_Meal_Plans (
                                    meal_plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    trainer_id INTEGER NOT NULL,
                                    member_id INTEGER NOT NULL,
                                    plan_date DATE NOT NULL,
                                    meal_type TEXT NOT NULL,
                                    meal_time TIME,
                                    foods TEXT,
                                    total_calories INTEGER,
                                    total_protein REAL,
                                    total_carbs REAL,
                                    total_fats REAL,
                                    preparation_instructions TEXT,
                                    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                    FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- CORRECTED TRAINER SALARIES TABLE
CREATE TABLE Trainer_Salaries (
                                  salary_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                  trainer_id INTEGER NOT NULL,
                                  month INTEGER NOT NULL,
                                  year INTEGER NOT NULL,
                                  base_salary REAL NOT NULL,
                                  bonus REAL DEFAULT 0.0,
                                  deductions REAL DEFAULT 0.0,
                                  net_salary REAL NOT NULL,
                                  status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'PAID')),
                                  payment_date TEXT,
                                  processed_by_admin_id INTEGER NOT NULL,
                                  created_date TEXT NOT NULL,
                                  last_modified TEXT,
                                  FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id) ON DELETE CASCADE,
                                  FOREIGN KEY (processed_by_admin_id) REFERENCES Admins(admin_id),
                                  UNIQUE(trainer_id, month, year)
);

CREATE TABLE Trainer_Member_Messages (
                                         message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         sender_id INTEGER NOT NULL,
                                         receiver_id INTEGER NOT NULL,
                                         sender_type TEXT NOT NULL,
                                         message_text TEXT NOT NULL,
                                         sent_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         read_status INTEGER DEFAULT 0,
                                         read_date DATETIME
);

-- MEMBER TABLES

CREATE TABLE Members (
                         member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                         username TEXT NOT NULL UNIQUE,
                         password_hash TEXT NOT NULL,
                         full_name TEXT NOT NULL,
                         email TEXT NOT NULL UNIQUE,
                         phone TEXT,
                         date_of_birth DATE,
                         gender TEXT,
                         profile_photo TEXT,
                         membership_type TEXT NOT NULL,
                         membership_start DATE NOT NULL,
                         membership_end DATE NOT NULL,
                         account_status TEXT NOT NULL,
                         created_by_admin_id INTEGER,
                         created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         last_login DATETIME,
                         FOREIGN KEY (created_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Member_Profiles (
                                 profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 member_id INTEGER NOT NULL UNIQUE,
                                 height REAL,
                                 current_weight REAL,
                                 target_weight REAL,
                                 fitness_level TEXT,
                                 primary_goal TEXT,
                                 medical_notes TEXT,
                                 emergency_contact TEXT,
                                 privacy_setting TEXT,
                                 last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- FITNESS TRACKING TABLES

CREATE TABLE Goals (
                       goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                       member_id INTEGER NOT NULL,
                       goal_type TEXT NOT NULL,
                       goal_category TEXT,
                       description TEXT,
                       target_value REAL,
                       current_value REAL,
                       start_date DATE NOT NULL,
                       end_date DATE,
                       status TEXT NOT NULL,
                       achievement_date DATE,
                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Workouts (
                          workout_id INTEGER PRIMARY KEY AUTOINCREMENT,
                          member_id INTEGER NOT NULL,
                          workout_date DATE NOT NULL,
                          workout_time TIME,
                          workout_type TEXT,
                          total_duration INTEGER,
                          total_calories INTEGER,
                          difficulty_rating INTEGER,
                          notes TEXT,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Exercises_Library (
                                   exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   exercise_name TEXT NOT NULL UNIQUE,
                                   category TEXT,
                                   muscle_group TEXT,
                                   equipment_needed TEXT,
                                   difficulty_level TEXT,
                                   instructions TEXT,
                                   video_link TEXT
);

CREATE TABLE Workout_Exercises (
                                   workout_exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   workout_id INTEGER NOT NULL,
                                   exercise_id INTEGER NOT NULL,
                                   sets INTEGER,
                                   reps INTEGER,
                                   weight REAL,
                                   duration INTEGER,
                                   order_number INTEGER,
                                   FOREIGN KEY (workout_id) REFERENCES Workouts(workout_id),
                                   FOREIGN KEY (exercise_id) REFERENCES Exercises_Library(exercise_id)
);

CREATE TABLE Meals (
                       meal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                       member_id INTEGER NOT NULL,
                       meal_type TEXT NOT NULL,
                       meal_date DATE NOT NULL,
                       meal_time TIME,
                       total_calories INTEGER,
                       total_protein REAL,
                       total_carbs REAL,
                       total_fats REAL,
                       notes TEXT,
                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Foods_Database (
                                food_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                food_name TEXT NOT NULL,
                                serving_size TEXT,
                                calories_per_serving INTEGER,
                                protein REAL,
                                carbs REAL,
                                fats REAL,
                                category TEXT,
                                is_gym_recommended INTEGER DEFAULT 0
);

CREATE TABLE Meal_Items (
                            meal_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            meal_id INTEGER NOT NULL,
                            food_id INTEGER NOT NULL,
                            quantity REAL NOT NULL,
                            unit TEXT,
                            calculated_calories INTEGER,
                            FOREIGN KEY (meal_id) REFERENCES Meals(meal_id),
                            FOREIGN KEY (food_id) REFERENCES Foods_Database(food_id)
);

CREATE TABLE Water_Logs (
                            water_log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            member_id INTEGER NOT NULL,
                            log_date DATE NOT NULL,
                            amount_ml INTEGER NOT NULL,
                            log_time TIME,
                            daily_total INTEGER,
                            FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Body_Measurements (
                                   measurement_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   member_id INTEGER NOT NULL,
                                   measurement_date DATE NOT NULL,
                                   weight REAL,
                                   chest REAL,
                                   waist REAL,
                                   hips REAL,
                                   arms REAL,
                                   legs REAL,
                                   body_fat_percentage REAL,
                                   FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- SOCIAL TABLES

CREATE TABLE Friendships (
                             friendship_id INTEGER PRIMARY KEY AUTOINCREMENT,
                             member_id_1 INTEGER NOT NULL,
                             member_id_2 INTEGER NOT NULL,
                             status TEXT NOT NULL,
                             requested_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                             accepted_date DATETIME,
                             FOREIGN KEY (member_id_1) REFERENCES Members(member_id),
                             FOREIGN KEY (member_id_2) REFERENCES Members(member_id)
);

CREATE TABLE Social_Activities (
                                   activity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   member_id INTEGER NOT NULL,
                                   activity_type TEXT NOT NULL,
                                   activity_description TEXT,
                                   visibility TEXT NOT NULL,
                                   timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   likes_count INTEGER DEFAULT 0,
                                   FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- INDEXES

CREATE INDEX idx_admin_logs_admin_id ON Admin_Logs(admin_id);
CREATE INDEX idx_admin_logs_timestamp ON Admin_Logs(timestamp);
CREATE INDEX idx_trainers_email ON Trainers(email);
CREATE INDEX idx_trainers_status ON Trainers(account_status);
CREATE INDEX idx_trainer_applications_status ON Trainer_Applications(status);
CREATE INDEX idx_trainer_member_assignment_trainer ON Trainer_Member_Assignment(trainer_id);
CREATE INDEX idx_trainer_member_assignment_member ON Trainer_Member_Assignment(member_id);
CREATE INDEX idx_trainer_workout_plans_trainer ON Trainer_Workout_Plans(trainer_id);
CREATE INDEX idx_trainer_workout_plans_member ON Trainer_Workout_Plans(member_id);
CREATE INDEX idx_trainer_daily_goals_member ON Trainer_Daily_Goals(member_id);
CREATE INDEX idx_trainer_meal_plans_member ON Trainer_Meal_Plans(member_id);
CREATE INDEX idx_trainer_salaries_trainer ON Trainer_Salaries(trainer_id);
CREATE INDEX idx_trainer_salaries_status ON Trainer_Salaries(status);
CREATE INDEX idx_trainer_salaries_month_year ON Trainer_Salaries(month, year);
CREATE INDEX idx_trainer_messages_sender ON Trainer_Member_Messages(sender_id);
CREATE INDEX idx_trainer_messages_receiver ON Trainer_Member_Messages(receiver_id);
CREATE INDEX idx_members_email ON Members(email);
CREATE INDEX idx_members_status ON Members(account_status);
CREATE INDEX idx_goals_member ON Goals(member_id);
CREATE INDEX idx_workouts_member ON Workouts(member_id);
CREATE INDEX idx_workouts_date ON Workouts(workout_date);
CREATE INDEX idx_workout_exercises_workout ON Workout_Exercises(workout_id);
CREATE INDEX idx_meals_member ON Meals(member_id);
CREATE INDEX idx_meals_date ON Meals(meal_date);
CREATE INDEX idx_meal_items_meal ON Meal_Items(meal_id);
CREATE INDEX idx_water_logs_member ON Water_Logs(member_id);
CREATE INDEX idx_water_logs_date ON Water_Logs(log_date);
CREATE INDEX idx_body_measurements_member ON Body_Measurements(member_id);
CREATE INDEX idx_friendships_member1 ON Friendships(member_id_1);
CREATE INDEX idx_friendships_member2 ON Friendships(member_id_2);
CREATE INDEX idx_social_activities_member ON Social_Activities(member_id);

-- SAMPLE DATA
-- Step 1: Delete all exercises
DELETE FROM Exercises_Library WHERE 1=1;

-- Step 2: Reset the autoincrement counter
DELETE FROM sqlite_sequence WHERE name='Exercises_Library';

-- Step 3: Insert exercises (IDs will start from 1)
INSERT INTO Exercises_Library (exercise_name, category, muscle_group, equipment_needed, difficulty_level, instructions) VALUES
                                                                                                                            ('Barbell Bench Press', 'Strength', 'Chest', 'Barbell, Bench', 'Intermediate', 'Lie on bench, lower bar to chest, press up'),
                                                                                                                            ('Dumbbell Bench Press', 'Strength', 'Chest', 'Dumbbells, Bench', 'Beginner', 'Lie on bench, press dumbbells up'),
                                                                                                                            ('Incline Bench Press', 'Strength', 'Chest', 'Barbell, Incline Bench', 'Intermediate', 'Press on incline bench, targets upper chest'),
                                                                                                                            ('Decline Bench Press', 'Strength', 'Chest', 'Barbell, Decline Bench', 'Intermediate', 'Press on decline bench, targets lower chest'),
                                                                                                                            ('Push-Ups', 'Bodyweight', 'Chest', 'None', 'Beginner', 'Standard push-up position, lower and press up'),
                                                                                                                            ('Dumbbell Flyes', 'Strength', 'Chest', 'Dumbbells, Bench', 'Intermediate', 'Lie on bench, arc dumbbells out and in'),
                                                                                                                            ('Cable Crossover', 'Strength', 'Chest', 'Cable Machine', 'Intermediate', 'Pull cables across body in arc motion'),
                                                                                                                            ('Dips (Chest)', 'Bodyweight', 'Chest', 'Dip Station', 'Intermediate', 'Lean forward, lower and press up'),
                                                                                                                            ('Deadlift', 'Strength', 'Back', 'Barbell', 'Advanced', 'Lift barbell from ground to standing position'),
                                                                                                                            ('Pull-Ups', 'Bodyweight', 'Back', 'Pull-up Bar', 'Intermediate', 'Hang from bar, pull body up'),
                                                                                                                            ('Barbell Row', 'Strength', 'Back', 'Barbell', 'Intermediate', 'Bent over, pull barbell to torso'),
                                                                                                                            ('Dumbbell Row', 'Strength', 'Back', 'Dumbbell, Bench', 'Beginner', 'One arm row, pull dumbbell to hip'),
                                                                                                                            ('Lat Pulldown', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull bar down to chest'),
                                                                                                                            ('Seated Cable Row', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull cable to torso'),
                                                                                                                            ('T-Bar Row', 'Strength', 'Back', 'T-Bar', 'Intermediate', 'Pull T-bar to chest'),
                                                                                                                            ('Face Pulls', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull rope to face level'),
                                                                                                                            ('Barbell Squat', 'Strength', 'Legs', 'Barbell, Rack', 'Intermediate', 'Lower into squat, press back up'),
                                                                                                                            ('Front Squat', 'Strength', 'Legs', 'Barbell, Rack', 'Advanced', 'Squat with bar on front shoulders'),
                                                                                                                            ('Leg Press', 'Strength', 'Legs', 'Leg Press Machine', 'Beginner', 'Push platform away with legs'),
                                                                                                                            ('Lunges', 'Strength', 'Legs', 'Dumbbells (optional)', 'Beginner', 'Step forward, lower back knee'),
                                                                                                                            ('Romanian Deadlift', 'Strength', 'Legs', 'Barbell', 'Intermediate', 'Hinge at hips, lower barbell'),
                                                                                                                            ('Leg Curl', 'Strength', 'Legs', 'Leg Curl Machine', 'Beginner', 'Curl legs up towards glutes'),
                                                                                                                            ('Leg Extension', 'Strength', 'Legs', 'Leg Extension Machine', 'Beginner', 'Extend legs from bent position'),
                                                                                                                            ('Calf Raises', 'Strength', 'Legs', 'Machine or Dumbbells', 'Beginner', 'Rise up on toes'),
                                                                                                                            ('Bulgarian Split Squat', 'Strength', 'Legs', 'Dumbbells, Bench', 'Intermediate', 'Rear foot elevated, squat down'),
                                                                                                                            ('Overhead Press', 'Strength', 'Shoulders', 'Barbell', 'Intermediate', 'Press barbell overhead'),
                                                                                                                            ('Dumbbell Shoulder Press', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Press dumbbells overhead'),
                                                                                                                            ('Lateral Raises', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Raise arms to sides'),
                                                                                                                            ('Front Raises', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Raise arms to front'),
                                                                                                                            ('Rear Delt Flyes', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Bend over, raise arms to sides'),
                                                                                                                            ('Arnold Press', 'Strength', 'Shoulders', 'Dumbbells', 'Intermediate', 'Rotating shoulder press'),
                                                                                                                            ('Upright Row', 'Strength', 'Shoulders', 'Barbell', 'Intermediate', 'Pull bar up along body'),
                                                                                                                            ('Barbell Curl', 'Strength', 'Arms', 'Barbell', 'Beginner', 'Curl barbell to shoulders'),
                                                                                                                            ('Dumbbell Curl', 'Strength', 'Arms', 'Dumbbells', 'Beginner', 'Curl dumbbells to shoulders'),
                                                                                                                            ('Hammer Curl', 'Strength', 'Arms', 'Dumbbells', 'Beginner', 'Curl with neutral grip'),
                                                                                                                            ('Tricep Dips', 'Bodyweight', 'Arms', 'Dip Station', 'Beginner', 'Lower and press up, arms focus'),
                                                                                                                            ('Tricep Pushdown', 'Strength', 'Arms', 'Cable Machine', 'Beginner', 'Push cable down, extend arms'),
                                                                                                                            ('Overhead Tricep Extension', 'Strength', 'Arms', 'Dumbbell', 'Beginner', 'Extend arms overhead'),
                                                                                                                            ('Skull Crushers', 'Strength', 'Arms', 'Barbell, Bench', 'Intermediate', 'Lower bar to forehead, extend'),
                                                                                                                            ('Cable Curl', 'Strength', 'Arms', 'Cable Machine', 'Beginner', 'Curl cable to shoulders'),
                                                                                                                            ('Plank', 'Bodyweight', 'Core', 'None', 'Beginner', 'Hold push-up position, engage core'),
                                                                                                                            ('Crunches', 'Bodyweight', 'Core', 'None', 'Beginner', 'Lift shoulders off ground'),
                                                                                                                            ('Russian Twists', 'Bodyweight', 'Core', 'None', 'Beginner', 'Rotate torso side to side'),
                                                                                                                            ('Leg Raises', 'Bodyweight', 'Core', 'None', 'Intermediate', 'Raise legs from lying position'),
                                                                                                                            ('Mountain Climbers', 'Bodyweight', 'Core', 'None', 'Intermediate', 'Alternate bringing knees to chest'),
                                                                                                                            ('Ab Wheel Rollout', 'Bodyweight', 'Core', 'Ab Wheel', 'Advanced', 'Roll wheel forward and back'),
                                                                                                                            ('Cable Woodchop', 'Strength', 'Core', 'Cable Machine', 'Intermediate', 'Rotate torso pulling cable'),
                                                                                                                            ('Hanging Knee Raises', 'Bodyweight', 'Core', 'Pull-up Bar', 'Intermediate', 'Raise knees while hanging'),
                                                                                                                            ('Treadmill Running', 'Cardio', 'Full Body', 'Treadmill', 'Beginner', 'Run at steady pace'),
                                                                                                                            ('Cycling', 'Cardio', 'Legs', 'Bike', 'Beginner', 'Pedal at steady pace'),
                                                                                                                            ('Rowing Machine', 'Cardio', 'Full Body', 'Rowing Machine', 'Beginner', 'Pull and push in rowing motion'),
                                                                                                                            ('Jump Rope', 'Cardio', 'Full Body', 'Jump Rope', 'Beginner', 'Jump continuously'),
                                                                                                                            ('Burpees', 'Cardio', 'Full Body', 'None', 'Intermediate', 'Squat, jump, push-up, repeat'),
                                                                                                                            ('Box Jumps', 'Cardio', 'Legs', 'Box', 'Intermediate', 'Jump onto elevated platform'),
                                                                                                                            ('Battle Ropes', 'Cardio', 'Full Body', 'Battle Ropes', 'Intermediate', 'Wave ropes up and down');

-- Insert default admin (password: admin123)
INSERT INTO Admins (username, password_hash, full_name, email, phone, role, account_status, created_date)
VALUES ('admin', '$2a$10$xqYLkkP0RMKF0YdKhZGzAeZ. GD5YqWXHqVr3GlqN5o5yVqYqB6rqe',
        'System Administrator', 'admin@gym.com', '1234567890', 'SUPER_ADMIN', 'ACTIVE', datetime('now'));

-- Enable foreign keys
PRAGMA foreign_keys = ON;
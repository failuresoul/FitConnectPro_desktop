-- ADMIN TABLES

CREATE TABLE Admins (
                        admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        phone TEXT,
                        role TEXT NOT NULL,
                        account_status TEXT DEFAULT 'ACTIVE',                    -- ← ADDED THIS LINE
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

CREATE TABLE Trainer_Salaries (
                                  salary_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                  trainer_id INTEGER NOT NULL,
                                  month INTEGER NOT NULL,
                                  year INTEGER NOT NULL,
                                  base_salary REAL NOT NULL,
                                  bonus REAL DEFAULT 0,
                                  deductions REAL DEFAULT 0,
                                  net_salary REAL NOT NULL,
                                  payment_date DATE,
                                  payment_status TEXT NOT NULL,
                                  processed_by_admin_id INTEGER,
                                  notes TEXT,
                                  FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                  FOREIGN KEY (processed_by_admin_id) REFERENCES Admins(admin_id)
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

-- Insert default admin (password: admin123)
INSERT INTO Admins (username, password_hash, full_name, email, phone, role, account_status, created_date)
VALUES ('admin', '$2a$10$xqYLkkP0RMKF0YdKhZGzAeZ. GD5YqWXHqVr3GlqN5o5yVqYqB6rqe',
        'System Administrator', 'admin@gym. com', '1234567890', 'SUPER_ADMIN', 'ACTIVE', datetime('now'));

-- Enable foreign keys
PRAGMA foreign_keys = ON;
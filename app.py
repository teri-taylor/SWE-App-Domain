from flask import Flask, request, jsonify
import sqlite3
import gunicorn
import os
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail


app= Flask(__name__)

connection = sqlite3.connect("users.db")
cursor = connection.cursor()

cursor.execute("""
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        security_question TEXT,
        security_answer TEXT,
        role TEXT DEFAULT 'user'
    )
""")

connection.commit()
connection.close()

def get_user(username):
    connection=sqlite3.connect('users.db')
    cursor=connection.cursor()
    cursor.execute(
        "SELECT username, password, email, security_question, security_answer, role FROM users WHERE username=?",
        (username,)
    )
    row=cursor.fetchone()
    connection.close()
    return row

@app.route('/login', methods=['POST'])
def login():
    data= request.get_json()
    username= data.get("username")
    password=data.get("password")
    user= get_user(username)
    if user and user[1] == password:  # index 1 = password
        return jsonify({"message": "Login successful!"}), 200
    else:
        return jsonify({"message": "Invalid credentials"}), 401

@app.route('/register', methods=['POST'])
def register():
    data=request.get_json()
    username=data.get("username")
    password=data.get("password")
    email=data.get("email")
    security_question=data.get("security_question")
    security_answer=data.get("security_answer")
    role=data.get("role")

    #checking if username exists
    connection=sqlite3.connect("users.db")
    cursor= connection.cursor()
    cursor.execute("SELECT 1 FROM users WHERE username=?", (username,))
    if cursor.fetchone():
        connection.close()
        return jsonify({"Username already exists"}),400
    cursor.execute(
        "INSERT INTO USERS (username, password, email, security_question, security_answer, role) VALUES (?,?,?,?,?,?)",
    (username, password, email, security_question, security_answer, role))
    connection.commit()
    connection.close()
    email_approval()
    return jsonify({"message": "Registration is complete. You have received a confirmation email."}), 201

def email_approval(to_email):
    #sends approval email when user succesfully registers
    message= Mail(
        from_email=os.environ.get("SENDGRID_SENDER"),
        to_emails=to_email,
        subject="Approval Notification",
        email_content="<strong>Your registration has been approved!</strong>"
    )
    try:
        sg=SendGridAPIClient(os.environ.get("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"))
        sg.send(message)
    except Exception as e:
        print(f"Sorry we had trouble sending email: {e}")

@app.route('/forgot-password', methods=['POST'])
def forgot_password():
    #verifies user identity with email username and security questions
    data=request.get_json()
    username=data.get("username")
    email=data.get("email")
    security_answer=data.get("security_answer")

    user=get_user(username)
    if not user:
        return jsonify({"message": "User not found"}), 404

    user_email= user[2]
    user_answer=user[4]

    if email != user_email or security_answer.lower() != user_answer.lower():
        return jsonify({"message": "Validation failed"}), 401

    return jsonify({"message": "Validation succesful. Please reset your password."}), 200


def update_user_password(username, new_password):
    connection=sqlite3.connect("users.db")
    cursor= connection.cursor()
    cursor.execute("UPDATE users SET password=? WHERE username=?", (new_password, username))
    connection.commit()
    connection.close()




@app.route('/reset-password', methods=['POST'])
def reset_password():
    data=request.get_json()
    username=data.get("username")
    new_password=data.get("new_password")

    if not is_password_good(new_password):
        return jsonify({"message": "Password must be 7 characters long and include uppercase, lowercase, and a number."}), 400

    user= get_user(username)
    if not user:
        return jsonify({"message":"User not found"}), 404

    update_user_password(username, new_password)
    return jsonify({"message": "Password changed succesfully"}),200

def is_password_good(password):
    if len(password)<7:
        return False
    if not any(char.isupper() for char in password):
        return False
    if not any(char.islower() for char in password):
        return False
    if not any(char.isdigit() for char in password):
        return False
    return True


if __name__ == "__main__":
    app.run(debug=True, port=5001)







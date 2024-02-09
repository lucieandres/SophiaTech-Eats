Feature: Customer Account Operations

  Scenario: Try to pass an order without register/login
    Given A non registered user
    When He tries to pass an order
    Then He get an error message and the order didn't succeed

  Scenario: Try to Register
    Given A non registered user
    When He tries to register himself with name "Axel" "Delille" and password "test"
    Then He's successfully registered and logged into the system

  Scenario: Successfully login
    Given A non registered user
    When He tries to login with name "user1" "name1" and password "password1"
    Then He's successfully logged into the system

  Scenario Outline: Try to login with wrong credentials
    Given A non registered user
    When He tries to login with name "<username>" "<name>" and password "<password>"
    Then The login didn't succeed

    Examples:
    | username      | name      | password      |
    | wrongUsername | name1     | password1     |
    | user2         | wrongName | password2     |
    | user3         | name3     | wrongPassword |


/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

#include "YicesSolver.h"

using namespace std;

YicesSolver::YicesSolver() 
{
    context = yices_mk_context();
    intType = yices_mk_type(context, "int");
    booleanType = yices_mk_type(context, "bool");
    solution = "empty";
}

void 
YicesSolver::solve (string constraint)
{
    int i = 0;
    int location = 0;
    int start = 0;

    solution = "empty";
    yices_reset(context);
    context = yices_mk_context();
    intType = yices_mk_type(context, "int");
    booleanType = yices_mk_type(context, "bool");
    
    while (i != string::npos) 
    {
        i = constraint.find("&", location);
        buildConstraint(constraint.substr(location, i - location));
        location = i + 1;
    }

    solveContext();
}

void 
YicesSolver::buildConstraint (string constraint) 
{
    yices_expr exp1;
    yices_expr exp2;
    yices_expr result;

    int comparator = 0;
    int location = 0;

    buildExpression(constraint, location, exp1);
    buildExpression(constraint, location, exp2);
    
    //TODO: find better implementation. 
    if (constraint[0] == '<' && constraint[1] == ' ')  
      result = yices_mk_lt(context, exp1, exp2);
    else if (constraint[0] == '>' && constraint[1] == ' ')
      result = yices_mk_gt(context, exp1, exp2);
    else if (constraint[0] == '<' && constraint[1] == '=')
      result = yices_mk_le(context, exp1, exp2);
    else if (constraint[0] == '>' && constraint[1] == '=')
      result = yices_mk_ge(context, exp1, exp2);
    else if (constraint[0] == '=' && constraint[1] == '=')
      result = yices_mk_eq(context, exp1, exp2);
    else if (constraint[0] == '!' && constraint[1] == '=')
      result = yices_mk_diseq(context, exp1, exp2);
    else
      cout << "ERROR: invalid path constraint or unsupported arithmetic operator (buildConstraint)\n";

    yices_assert(context, result);
}

void 
YicesSolver::buildExpression(const string& constraint, int& location, yices_expr& expression)
{
    string token;
    int i = 0;
    bool hasAnswer = false;
    string temp;

    i = constraint.find(" ", location);
    token = constraint.substr(location, i-location);
    location = i + 1;

    if (isNumber(token))
    {
        char *temp;
        
        temp = new char[token.length() + 1];
        strcpy(temp, token.c_str());
        
        expression = yices_mk_num_from_string(context, temp);

        delete[] temp;
    }
    else if (token.compare("true") == 0)
        expression = yices_mk_true(context);
    else if (token.compare("false") == 0)
        expression = yices_mk_false(context);
    else if (isVariable(token)) 
    {
        list<yices_var_decl>::iterator iter;
        
        for (iter = variables.begin(); iter != variables.end(); iter++) {
            temp = yices_get_var_decl_name(*iter);
            if (token == temp) 
            {
                expression = yices_mk_var_from_decl(context, *iter);
                hasAnswer = true;
                iter = variables.end();
                iter--;
            }
        }
        
        if (hasAnswer == false)
        {
            char *temp;
            temp = new char[token.length() + 1];
            strcpy(temp, token.c_str());

            //TODO: add boolean type variable
            yices_var_decl newVariable = yices_mk_var_decl(context, temp, intType); 
            
            expression = yices_mk_var_from_decl(context, newVariable);
            variables.push_back(newVariable);

            delete[] temp;
        }
    }
    else if (token[0] == '+')
    {
        yices_expr args[2];
        
        buildExpression(constraint, location, args[0]);
        buildExpression(constraint, location, args[1]);
        
        expression = yices_mk_sum(context, args, 2);
    }
    else if (token[0] == '-') 
    {
        yices_expr args[2];
        
        buildExpression(constraint, location, args[0]);
        buildExpression(constraint, location, args[1]);
        
        expression = yices_mk_sub(context, args, 2);
    }
    else if (token[0] == '*') 
    {
        yices_expr args[2];
        
        buildExpression(constraint, location, args[0]);
        buildExpression(constraint, location, args[1]);
        
        expression = yices_mk_mul(context, args, 2);
    }
    else if (token[0] == '/') 
    {
        yices_expr args[2];
        
        buildExpression(constraint, location, args[0]);
        buildExpression(constraint, location, args[1]);
        
        yices_var_decl div_decl = yices_get_var_decl_from_name(context, "div");
        yices_expr div = yices_mk_var_from_decl(context, div_decl);

        expression = yices_mk_app(context, div, args, 2);
    }
    //TODO: add additional case for boolean type
    else 
    {
        cout << "ERROR: invalid path constraint or unsupported arithmetic operator (buildExpression)\n";
        cout << token << "\n";
    }
}

bool 
YicesSolver::isNumber (const string& str)
{
    if (str[0] >= '0' && str [0] <= '9')
        return true;

    if (str.length() > 1)
        if (str[0] == '-' && str[1] >= '0' && str[1] <= '9')
            return true;
    return false;
}

bool 
YicesSolver::isVariable (const string& str)
{
    //TODO: give more flexibility
    if ((str[0] >= 'a' && str[0] <= 'z') || (str[0] >= 'A' && str[0] <= 'Z'))
        return true;
    return false;
}

void
YicesSolver::solveContext()
{
    lbool satisfiability;
    yices_model model;
    list<yices_var_decl>::iterator iter;
    long* value;

    satisfiability = yices_check(context);
    model = yices_get_model(context);
    solution = "";
    *value = 0;

    switch (satisfiability) 
    {
        case l_true:
            for (iter = variables.begin(); iter != variables.end(); iter++) 
            {
                const char *name = yices_get_var_decl_name(*iter);
                lbool bvalue;
                ostringstream o;

                //TODO: add additional case for boolean type
                yices_get_int_value(model, *iter, value);
                o << *value;

                solution = solution + name;
                solution = solution + " " + o.str() + " ";
            }
           break;
        case l_false:
          solution = "unsat";
          break;
        case l_undef:
          solution = "unknown";
          break;
    }
}

string 
YicesSolver::getSolution()
{
    return solution;
}





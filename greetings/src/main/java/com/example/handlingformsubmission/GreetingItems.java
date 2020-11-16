package com.example.handlingformsubmission;

public class GreetingItems {
	// Set up data members that correspond to columns in the Work table
			private String id;
			private String name;
			private String message;
			private String title;

			public GreetingItems()
			{
			}

			public String getId() {
				return this.id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getName() {
				return this.name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getMessage(){
				return this.message;
			}

			public void setMessage(String message){
				this.message = message;
			}

			public String getTitle() {
				return this.title;
			}

			public void setTitle(String title) {
				this.title = title;
			}
}

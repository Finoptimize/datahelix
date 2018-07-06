import * as React from "react";

import {Form, Grid, Icon} from "semantic-ui-react";
import AddEmptyFieldButton from "./AddEmptyFieldButton";

interface IProps
{
	children: React.ReactNode[];
}

const ProfileEditor = ({children}: IProps) =>
	<Form fluid="true">
		<Grid columns={3} divided="vertically">
			{ children }

			<Grid.Row>
				<Grid.Column width={1}>
					<AddEmptyFieldButton icon={true}>
						<Icon name="plus" />
					</AddEmptyFieldButton>
				</Grid.Column>
			</Grid.Row>
		</Grid>
	</Form>

export default ProfileEditor;